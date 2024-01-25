package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.Main;
import hudson.remoting.ClassFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ClassFilterImpl extends ClassFilter {
  private static final Logger LOGGER = Logger.getLogger(ClassFilterImpl.class.getName());
  
  private static boolean SUPPRESS_WHITELIST = SystemProperties.getBoolean("jenkins.security.ClassFilterImpl.SUPPRESS_WHITELIST");
  
  private static boolean SUPPRESS_ALL = SystemProperties.getBoolean("jenkins.security.ClassFilterImpl.SUPPRESS_ALL");
  
  private static final String JENKINS_LOC = codeSource(jenkins.model.Jenkins.class);
  
  private static final String REMOTING_LOC = codeSource(ClassFilter.class);
  
  public static void register() {
    if (Main.isUnitTest && JENKINS_LOC == null) {
      mockOff();
      return;
    } 
    ClassFilter.setDefault(new ClassFilterImpl());
    if (SUPPRESS_ALL) {
      LOGGER.warning("All class filtering suppressed. Your Jenkins installation is at risk from known attacks. See https://www.jenkins.io/redirect/class-filter/");
    } else if (SUPPRESS_WHITELIST) {
      LOGGER.warning("JEP-200 class filtering by whitelist suppressed. Your Jenkins installation may be at risk. See https://www.jenkins.io/redirect/class-filter/");
    } 
  }
  
  public static void unregister() { ClassFilter.setDefault(ClassFilter.STANDARD); }
  
  private static void mockOff() {
    LOGGER.warning("Disabling class filtering since we appear to be in a special test environment, perhaps Mockito");
    ClassFilter.setDefault(ClassFilter.NONE);
  }
  
  private final Map<Class<?>, Boolean> cache = Collections.synchronizedMap(new WeakHashMap());
  
  private final Map<String, Boolean> codeSourceCache = Collections.synchronizedMap(new HashMap());
  
  static final Set<String> WHITELISTED_CLASSES;
  
  private static final Pattern CLASSES_JAR;
  
  static  {
    try {
      is = ClassFilterImpl.class.getResourceAsStream("whitelisted-classes.txt");
      try {
        WHITELISTED_CLASSES = (Set)IOUtils.readLines(is, StandardCharsets.UTF_8).stream().filter(line -> !line.matches("#.*|\\s*")).collect(Collectors.toUnmodifiableSet());
        if (is != null)
          is.close(); 
      } catch (Throwable throwable) {
        if (is != null)
          try {
            is.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException x) {
      throw new ExceptionInInitializerError(x);
    } 
    CLASSES_JAR = Pattern.compile("(file:/.+/)WEB-INF/lib/classes[.]jar");
  }
  
  public boolean isBlacklisted(Class _c) {
    for (CustomClassFilter f : ExtensionList.lookup(CustomClassFilter.class)) {
      Boolean r = f.permits(_c);
      if (r != null) {
        if (r.booleanValue()) {
          LOGGER.log(Level.FINER, "{0} specifies a policy for {1}: {2}", new Object[] { f, _c.getName(), Boolean.valueOf(true) });
        } else {
          notifyRejected(_c, _c.getName(), String.format("%s specifies a policy for %s: %s ", new Object[] { f, _c.getName(), r }));
        } 
        return !r.booleanValue();
      } 
    } 
    return ((Boolean)this.cache.computeIfAbsent(_c, c -> {
          String name = c.getName();
          if (Main.isUnitTest && (name.contains("$$EnhancerByMockitoWithCGLIB$$") || name.contains("$$FastClassByMockitoWithCGLIB$$") || name.startsWith("org.mockito."))) {
            mockOff();
            return Boolean.valueOf(false);
          } 
          if (ClassFilter.STANDARD.isBlacklisted(c)) {
            notifyRejected(_c, _c.getName(), String.format("%s is not permitted ", new Object[] { _c.getName() }));
            return Boolean.valueOf(true);
          } 
          if (c.isArray()) {
            LOGGER.log(Level.FINE, "permitting {0} since it is an array", name);
            return Boolean.valueOf(false);
          } 
          if (Throwable.class.isAssignableFrom(c)) {
            LOGGER.log(Level.FINE, "permitting {0} since it is a throwable", name);
            return Boolean.valueOf(false);
          } 
          if (Enum.class.isAssignableFrom(c)) {
            LOGGER.log(Level.FINE, "permitting {0} since it is an enum", name);
            return Boolean.valueOf(false);
          } 
          String location = codeSource(c);
          if (location != null) {
            if (isLocationWhitelisted(location)) {
              LOGGER.log(Level.FINE, "permitting {0} due to its location in {1}", new Object[] { name, location });
              return Boolean.valueOf(false);
            } 
          } else {
            ClassLoader loader = c.getClassLoader();
            if (loader != null && loader.getClass().getName().equals("hudson.remoting.RemoteClassLoader")) {
              LOGGER.log(Level.FINE, "permitting {0} since it was loaded by a remote class loader", name);
              return Boolean.valueOf(false);
            } 
          } 
          if (WHITELISTED_CLASSES.contains(name)) {
            LOGGER.log(Level.FINE, "tolerating {0} by whitelist", name);
            return Boolean.valueOf(false);
          } 
          if (SUPPRESS_WHITELIST || SUPPRESS_ALL) {
            notifyRejected(_c, null, String.format("%s in %s might be dangerous, so would normally be rejected; see https://www.jenkins.io/redirect/class-filter/", new Object[] { name, (location != null) ? location : "JRE" }));
            return Boolean.valueOf(false);
          } 
          notifyRejected(_c, null, String.format("%s in %s might be dangerous, so rejecting; see https://www.jenkins.io/redirect/class-filter/", new Object[] { name, (location != null) ? location : "JRE" }));
          return Boolean.valueOf(true);
        })).booleanValue();
  }
  
  private boolean isLocationWhitelisted(String _loc) { return ((Boolean)this.codeSourceCache.computeIfAbsent(_loc, loc -> {
          if (loc.equals(JENKINS_LOC)) {
            LOGGER.log(Level.FINE, "{0} seems to be the location of Jenkins core, OK", loc);
            return Boolean.valueOf(true);
          } 
          if (loc.equals(REMOTING_LOC)) {
            LOGGER.log(Level.FINE, "{0} seems to be the location of Remoting, OK", loc);
            return Boolean.valueOf(true);
          } 
          if (loc.matches("file:/.+[.]jar"))
            try {
              JarFile jf = new JarFile(new File(new URI(loc)), false);
              try {
                Manifest mf = jf.getManifest();
                if (mf != null) {
                  if (isPluginManifest(mf)) {
                    LOGGER.log(Level.FINE, "{0} seems to be a Jenkins plugin, OK", loc);
                    Boolean bool = Boolean.valueOf(true);
                    jf.close();
                    return bool;
                  } 
                  LOGGER.log(Level.FINE, "{0} does not look like a Jenkins plugin", loc);
                } else {
                  LOGGER.log(Level.FINE, "ignoring {0} with no manifest", loc);
                } 
                jf.close();
              } catch (Throwable throwable) {
                try {
                  jf.close();
                } catch (Throwable throwable1) {
                  throwable.addSuppressed(throwable1);
                } 
                throw throwable;
              } 
            } catch (Exception x) {
              LOGGER.log(Level.WARNING, "problem checking " + loc, x);
            }  
          Matcher m = CLASSES_JAR.matcher(loc);
          if (m.matches())
            try {
              File manifestFile = new File(new URI(m.group(1) + "META-INF/MANIFEST.MF"));
              if (manifestFile.isFile()) {
                InputStream is = new FileInputStream(manifestFile);
                try {
                  if (isPluginManifest(new Manifest(is))) {
                    LOGGER.log(Level.FINE, "{0} looks like a Jenkins plugin based on {1}, OK", new Object[] { loc, manifestFile });
                    Boolean bool = Boolean.valueOf(true);
                    is.close();
                    return bool;
                  } 
                  LOGGER.log(Level.FINE, "{0} does not look like a Jenkins plugin", manifestFile);
                  is.close();
                } catch (Throwable throwable) {
                  try {
                    is.close();
                  } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                  } 
                  throw throwable;
                } 
              } else {
                LOGGER.log(Level.FINE, "{0} has no matching {1}", new Object[] { loc, manifestFile });
              } 
            } catch (Exception x) {
              LOGGER.log(Level.WARNING, "problem checking " + loc, x);
            }  
          if (loc.endsWith("/target/classes/") || loc.matches(".+/build/classes/[^/]+/main/")) {
            LOGGER.log(Level.FINE, "{0} seems to be current plugin classes, OK", loc);
            return Boolean.valueOf(true);
          } 
          if (Main.isUnitTest) {
            if (loc.endsWith("/target/test-classes/") || loc.endsWith("-tests.jar") || loc.matches(".+/build/classes/[^/]+/test/")) {
              LOGGER.log(Level.FINE, "{0} seems to be test classes, OK", loc);
              return Boolean.valueOf(true);
            } 
            if (loc.matches(".+/jenkins-test-harness-.+[.]jar")) {
              LOGGER.log(Level.FINE, "{0} seems to be jenkins-test-harness, OK", loc);
              return Boolean.valueOf(true);
            } 
          } 
          LOGGER.log(Level.FINE, "{0} is not recognized; rejecting", loc);
          return Boolean.valueOf(false);
        })).booleanValue(); }
  
  @CheckForNull
  private static String codeSource(@NonNull Class<?> c) {
    CodeSource cs = c.getProtectionDomain().getCodeSource();
    if (cs == null)
      return null; 
    URL loc = cs.getLocation();
    if (loc == null)
      return null; 
    String r = loc.toString();
    if (r.endsWith(".class")) {
      String suffix = c.getName().replace('.', '/') + ".class";
      if (r.endsWith(suffix))
        r = r.substring(0, r.length() - suffix.length()); 
    } 
    if (r.startsWith("jar:file:/") && r.endsWith(".jar!/"))
      r = r.substring(4, r.length() - 2); 
    return r;
  }
  
  private static boolean isPluginManifest(Manifest mf) {
    Attributes attr = mf.getMainAttributes();
    return ((attr.getValue("Short-Name") != null && (attr.getValue("Plugin-Version") != null || attr.getValue("Jenkins-Version") != null)) || "true"
      .equals(attr.getValue("Jenkins-ClassFilter-Whitelisted")));
  }
  
  public boolean isBlacklisted(String name) {
    if (Main.isUnitTest && name.contains("$$EnhancerByMockitoWithCGLIB$$")) {
      mockOff();
      return false;
    } 
    for (CustomClassFilter f : ExtensionList.lookup(CustomClassFilter.class)) {
      Boolean r = f.permits(name);
      if (r != null) {
        if (r.booleanValue()) {
          LOGGER.log(Level.FINER, "{0} specifies a policy for {1}: {2}", new Object[] { f, name, Boolean.valueOf(true) });
        } else {
          notifyRejected(null, name, 
              String.format("%s specifies a policy for %s: %s", new Object[] { f, name, r }));
        } 
        return !r.booleanValue();
      } 
    } 
    if (ClassFilter.STANDARD.isBlacklisted(name)) {
      if (SUPPRESS_ALL) {
        notifyRejected(null, name, 
            String.format("would normally reject %s according to standard blacklist; see https://www.jenkins.io/redirect/class-filter/", new Object[] { name }));
        return false;
      } 
      notifyRejected(null, name, 
          String.format("rejecting %s according to standard blacklist; see https://www.jenkins.io/redirect/class-filter/", new Object[] { name }));
      return true;
    } 
    return false;
  }
  
  private void notifyRejected(@CheckForNull Class<?> clazz, @CheckForNull String clazzName, String message) {
    Throwable cause = null;
    if (LOGGER.isLoggable(Level.FINE))
      cause = new SecurityException("Class rejected by the class filter: " + ((clazz != null) ? clazz.getName() : clazzName)); 
    LOGGER.log(Level.WARNING, message, cause);
  }
}
