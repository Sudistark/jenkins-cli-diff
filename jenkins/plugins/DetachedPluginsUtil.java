package jenkins.plugins;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.PluginWrapper;
import hudson.util.VersionNumber;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class DetachedPluginsUtil {
  private static final Logger LOGGER = Logger.getLogger(DetachedPluginsUtil.class.getName());
  
  @VisibleForTesting
  static final List<DetachedPlugin> DETACHED_LIST;
  
  private static final Set<String> BREAK_CYCLES;
  
  static  {
    try {
      is = hudson.ClassicPluginStrategy.class.getResourceAsStream("/jenkins/split-plugins.txt");
      try {
        DETACHED_LIST = (List)configLines(is).map(line -> {
              String[] pieces = line.split(" ");
              return new DetachedPlugin(pieces[0], pieces[1] + ".*", pieces[2]);
            }).collect(Collectors.toUnmodifiableList());
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
    try {
      is = hudson.ClassicPluginStrategy.class.getResourceAsStream("/jenkins/split-plugin-cycles.txt");
      try {
        BREAK_CYCLES = (Set)configLines(is).collect(Collectors.toUnmodifiableSet());
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
  }
  
  @NonNull
  public static List<PluginWrapper.Dependency> getImpliedDependencies(String pluginName, String jenkinsVersion) {
    List<PluginWrapper.Dependency> out = new ArrayList<PluginWrapper.Dependency>();
    for (DetachedPlugin detached : getDetachedPlugins()) {
      if (detached.shortName.equals(pluginName))
        continue; 
      if (BREAK_CYCLES.contains(pluginName + " " + pluginName)) {
        LOGGER.log(Level.FINE, "skipping implicit dependency {0} → {1}", new Object[] { pluginName, detached.shortName });
        continue;
      } 
      if (jenkinsVersion == null || jenkinsVersion.equals("null") || (new VersionNumber(jenkinsVersion)).compareTo(detached.splitWhen) <= 0) {
        out.add(new PluginWrapper.Dependency(detached.shortName + ":" + detached.shortName + ";resolution:=optional"));
        LOGGER.log(Level.FINE, "adding implicit dependency {0} → {1} because of {2}", new Object[] { pluginName, detached.shortName, jenkinsVersion });
      } 
    } 
    return out;
  }
  
  @NonNull
  public static List<DetachedPlugin> getDetachedPlugins() { return List.copyOf(DETACHED_LIST); }
  
  @NonNull
  public static List<DetachedPlugin> getDetachedPlugins(@NonNull VersionNumber since) {
    return (List)getDetachedPlugins().stream()
      .filter(detachedPlugin -> !detachedPlugin.getSplitWhen().isOlderThan(since))
      .collect(Collectors.toList());
  }
  
  public static boolean isDetachedPlugin(@NonNull String pluginId) {
    for (DetachedPlugin detachedPlugin : getDetachedPlugins()) {
      if (detachedPlugin.getShortName().equals(pluginId))
        return true; 
    } 
    return false;
  }
  
  public static Stream<String> configLines(InputStream is) throws IOException { return IOUtils.readLines(is, StandardCharsets.UTF_8).stream().filter(line -> !line.matches("#.*|\\s*")); }
}
