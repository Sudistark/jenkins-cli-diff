package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.init.InitMilestone;
import hudson.init.InitStrategy;
import hudson.init.InitializerFinder;
import hudson.model.AbstractModelObject;
import hudson.model.Api;
import hudson.model.Descriptor;
import hudson.model.DownloadService;
import hudson.model.Failure;
import hudson.model.Messages;
import hudson.model.UpdateCenter;
import hudson.model.UpdateSite;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.PersistedList;
import hudson.util.Retrier;
import hudson.util.Service;
import hudson.util.VersionNumber;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.CodeSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import jenkins.ExtensionRefreshException;
import jenkins.InitReactorRunner;
import jenkins.RestartRequiredException;
import jenkins.YesNoMaybe;
import jenkins.install.InstallState;
import jenkins.install.InstallUtil;
import jenkins.model.Jenkins;
import jenkins.plugins.DetachedPluginsUtil;
import jenkins.security.CustomClassFilter;
import jenkins.util.SystemProperties;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerOverridable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.core.Authentication;
import org.xml.sax.SAXException;

@ExportedBean
public abstract class PluginManager extends AbstractModelObject implements OnMaster, StaplerOverridable, StaplerProxy {
  public static final String CUSTOM_PLUGIN_MANAGER = PluginManager.class.getName() + ".className";
  
  private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
  
  static int CHECK_UPDATE_SLEEP_TIME_MILLIS;
  
  static int CHECK_UPDATE_ATTEMPTS;
  
  protected final List<PluginWrapper> plugins;
  
  protected final List<PluginWrapper> activePlugins;
  
  protected final List<FailedPlugin> failedPlugins;
  
  public final File rootDir;
  
  private String lastErrorCheckUpdateCenters;
  
  @CheckForNull
  private final File workDir;
  
  @Deprecated
  public final ServletContext context;
  
  public final ClassLoader uberClassLoader;
  
  private boolean pluginListed;
  
  private final PluginStrategy strategy;
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean FAST_LOOKUP;
  
  @Deprecated
  public static final Permission UPLOAD_PLUGINS;
  
  @Deprecated
  public static final Permission CONFIGURE_UPDATECENTER;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK;
  
  static  {
    try {
      CHECK_UPDATE_SLEEP_TIME_MILLIS = SystemProperties.getInteger(PluginManager.class.getName() + ".checkUpdateSleepTimeMillis", Integer.valueOf(1000)).intValue();
      CHECK_UPDATE_ATTEMPTS = SystemProperties.getInteger(PluginManager.class.getName() + ".checkUpdateAttempts", Integer.valueOf(1)).intValue();
    } catch (RuntimeException e) {
      LOGGER.warning(String.format("There was an error initializing the PluginManager. Exception: %s", new Object[] { e }));
    } finally {
      CHECK_UPDATE_ATTEMPTS = (CHECK_UPDATE_ATTEMPTS > 0) ? CHECK_UPDATE_ATTEMPTS : 1;
      CHECK_UPDATE_SLEEP_TIME_MILLIS = (CHECK_UPDATE_SLEEP_TIME_MILLIS > 0) ? CHECK_UPDATE_SLEEP_TIME_MILLIS : 1000;
    } 
    FAST_LOOKUP = !SystemProperties.getBoolean(PluginManager.class.getName() + ".noFastLookup");
    UPLOAD_PLUGINS = new Permission(Jenkins.PERMISSIONS, "UploadPlugins", Messages._PluginManager_UploadPluginsPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);
    CONFIGURE_UPDATECENTER = new Permission(Jenkins.PERMISSIONS, "ConfigureUpdateCenter", Messages._PluginManager_ConfigureUpdateCenterPermission_Description(), Jenkins.ADMINISTER, PermissionScope.JENKINS);
    SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(PluginManager.class.getName() + ".skipPermissionCheck");
  }
  
  @NonNull
  public static PluginManager createDefault(@NonNull Jenkins jenkins) {
    String pmClassName = SystemProperties.getString(CUSTOM_PLUGIN_MANAGER);
    if (!StringUtils.isBlank(pmClassName)) {
      LOGGER.log(Level.FINE, String.format("Use of custom plugin manager [%s] requested.", new Object[] { pmClassName }));
      try {
        Class<? extends PluginManager> klass = Class.forName(pmClassName).asSubclass(PluginManager.class);
        for (PMConstructor c : PMConstructor.values()) {
          PluginManager pm = c.create(klass, jenkins);
          if (pm != null)
            return pm; 
        } 
        LOGGER.log(Level.WARNING, String.format("Provided custom plugin manager [%s] does not provide any of the suitable constructors. Using default.", new Object[] { pmClassName }));
      } catch (ClassCastException e) {
        LOGGER.log(Level.WARNING, String.format("Provided class [%s] does not extend PluginManager. Using default.", new Object[] { pmClassName }));
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, String.format("Unable to instantiate custom plugin manager [%s]. Using default.", new Object[] { pmClassName }), e);
      } 
    } 
    return new LocalPluginManager(jenkins);
  }
  
  protected PluginManager(ServletContext context, File rootDir) {
    this.plugins = new CopyOnWriteArrayList();
    this.activePlugins = new CopyOnWriteArrayList();
    this.failedPlugins = new ArrayList();
    this.lastErrorCheckUpdateCenters = null;
    this.uberClassLoader = new UberClassLoader(this.activePlugins);
    this.pluginUploaded = false;
    this.pluginListed = false;
    this.context = context;
    this.rootDir = rootDir;
    try {
      Util.createDirectories(rootDir.toPath(), new java.nio.file.attribute.FileAttribute[0]);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } 
    String workDir = SystemProperties.getString(PluginManager.class.getName() + ".workDir");
    this.workDir = StringUtils.isBlank(workDir) ? null : new File(workDir);
    this.strategy = createPluginStrategy();
  }
  
  public Api getApi() {
    Jenkins.get().checkPermission(Jenkins.SYSTEM_READ);
    return new Api(this);
  }
  
  @CheckForNull
  public File getWorkDir() { return this.workDir; }
  
  public Collection<PluginManagerStaplerOverride> getOverrides() { return PluginManagerStaplerOverride.all(); }
  
  public TaskBuilder initTasks(InitStrategy initStrategy) {
    TaskBuilder builder;
    if (!this.pluginListed) {
      builder = new Object(this, initStrategy);
    } else {
      builder = TaskBuilder.EMPTY_BUILDER;
    } 
    InitializerFinder initializerFinder = new InitializerFinder(this.uberClassLoader);
    return TaskBuilder.union(new TaskBuilder[] { initializerFinder, builder, new Object(this, initializerFinder) });
  }
  
  void considerDetachedPlugin(String shortName) {
    if ((new File(this.rootDir, shortName + ".jpi")).isFile() || (new File(this.rootDir, shortName + ".hpi")).isFile() || (new File(this.rootDir, shortName + ".jpl")).isFile() || (new File(this.rootDir, shortName + ".hpl")).isFile()) {
      LOGGER.fine(() -> "not considering loading a detached dependency " + shortName + " as it is already on disk");
      return;
    } 
    LOGGER.fine(() -> "considering loading a detached dependency " + shortName);
    for (String loadedFile : loadPluginsFromWar(getDetachedLocation(), (dir, name) -> normalisePluginName(name).equals(shortName))) {
      String loaded = normalisePluginName(loadedFile);
      File arc = new File(this.rootDir, loaded + ".jpi");
      LOGGER.info(() -> "Loading a detached plugin as a dependency: " + arc);
      try {
        this.plugins.add(this.strategy.createPluginWrapper(arc));
      } catch (IOException e) {
        this.failedPlugins.add(new FailedPlugin(arc.getName(), e));
      } 
    } 
  }
  
  @NonNull
  protected String getDetachedLocation() { return "/WEB-INF/detached-plugins"; }
  
  @NonNull
  protected Set<String> loadPluginsFromWar(@NonNull String fromPath) { return loadPluginsFromWar(fromPath, null); }
  
  @SuppressFBWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification = "Plugin loading happens only once on Jenkins startup")
  @NonNull
  protected Set<String> loadPluginsFromWar(@NonNull String fromPath, @CheckForNull FilenameFilter filter) {
    Set<String> names = new HashSet<String>();
    ServletContext context = (Jenkins.get()).servletContext;
    Set<String> plugins = Util.fixNull(context.getResourcePaths(fromPath));
    Set<URL> copiedPlugins = new HashSet<URL>();
    Set<URL> dependencies = new HashSet<URL>();
    for (String pluginPath : plugins) {
      String fileName = pluginPath.substring(pluginPath.lastIndexOf('/') + 1);
      if (fileName.isEmpty())
        continue; 
      try {
        URL url = context.getResource(pluginPath);
        if (filter != null && url != null && !filter.accept((new File(url.getFile())).getParentFile(), fileName))
          continue; 
        names.add(fileName);
        copyBundledPlugin((URL)Objects.requireNonNull(url), fileName);
        copiedPlugins.add(url);
        try {
          addDependencies(url, fromPath, dependencies);
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Failed to resolve dependencies for the bundled plugin " + fileName, e);
        } 
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to extract the bundled plugin " + fileName, e);
      } 
    } 
    for (URL dependency : dependencies) {
      if (copiedPlugins.contains(dependency))
        continue; 
      String fileName = (new File(dependency.getFile())).getName();
      try {
        names.add(fileName);
        copyBundledPlugin(dependency, fileName);
        copiedPlugins.add(dependency);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to extract the bundled dependency plugin " + fileName, e);
      } 
    } 
    return names;
  }
  
  @SuppressFBWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification = "Plugin loading happens only once on Jenkins startup")
  protected static void addDependencies(URL hpiResUrl, String fromPath, Set<URL> dependencySet) throws URISyntaxException, MalformedURLException {
    if (dependencySet.contains(hpiResUrl))
      return; 
    Manifest manifest = parsePluginManifest(hpiResUrl);
    String dependencySpec = manifest.getMainAttributes().getValue("Plugin-Dependencies");
    if (dependencySpec != null) {
      String[] dependencyTokens = dependencySpec.split(",");
      ServletContext context = (Jenkins.get()).servletContext;
      for (String dependencyToken : dependencyTokens) {
        if (!dependencyToken.endsWith(";resolution:=optional")) {
          String[] artifactIdVersionPair = dependencyToken.split(":");
          String artifactId = artifactIdVersionPair[0];
          VersionNumber dependencyVersion = new VersionNumber(artifactIdVersionPair[1]);
          PluginManager manager = Jenkins.get().getPluginManager();
          VersionNumber installedVersion = manager.getPluginVersion(manager.rootDir, artifactId);
          if (installedVersion == null || installedVersion.isOlderThan(dependencyVersion)) {
            URL dependencyURL = context.getResource(fromPath + "/" + fromPath + ".hpi");
            if (dependencyURL == null)
              dependencyURL = context.getResource(fromPath + "/" + fromPath + ".jpi"); 
            if (dependencyURL != null) {
              addDependencies(dependencyURL, fromPath, dependencySet);
              dependencySet.add(dependencyURL);
            } 
          } 
        } 
      } 
    } 
  }
  
  protected void loadDetachedPlugins() {
    VersionNumber lastExecVersion = new VersionNumber(InstallUtil.getLastExecVersion());
    if (lastExecVersion.isNewerThan(InstallUtil.NEW_INSTALL_VERSION) && lastExecVersion.isOlderThan(Jenkins.getVersion())) {
      LOGGER.log(Level.INFO, "Upgrading Jenkins. The last running version was {0}. This Jenkins is version {1}.", new Object[] { lastExecVersion, Jenkins.VERSION });
      List<DetachedPluginsUtil.DetachedPlugin> detachedPlugins = DetachedPluginsUtil.getDetachedPlugins(lastExecVersion);
      Set<String> loadedDetached = loadPluginsFromWar(getDetachedLocation(), new Object(this, detachedPlugins));
      LOGGER.log(Level.INFO, "Upgraded Jenkins from version {0} to version {1}. Loaded detached plugins (and dependencies): {2}", new Object[] { lastExecVersion, Jenkins.VERSION, loadedDetached });
    } else {
      Set<DetachedPluginsUtil.DetachedPlugin> forceUpgrade = new HashSet<DetachedPluginsUtil.DetachedPlugin>();
      for (DetachedPluginsUtil.DetachedPlugin p : DetachedPluginsUtil.getDetachedPlugins()) {
        VersionNumber installedVersion = getPluginVersion(this.rootDir, p.getShortName());
        VersionNumber requiredVersion = p.getRequiredVersion();
        if (installedVersion != null && installedVersion.isOlderThan(requiredVersion)) {
          LOGGER.log(Level.WARNING, "Detached plugin {0} found at version {1}, required minimum version is {2}", new Object[] { p.getShortName(), installedVersion, requiredVersion });
          forceUpgrade.add(p);
        } 
      } 
      if (!forceUpgrade.isEmpty()) {
        Set<String> loadedDetached = loadPluginsFromWar(getDetachedLocation(), new Object(this, forceUpgrade));
        LOGGER.log(Level.INFO, "Upgraded detached plugins (and dependencies): {0}", new Object[] { loadedDetached });
      } 
    } 
  }
  
  private String normalisePluginName(@NonNull String name) { return name.replace(".jpi", "").replace(".hpi", ""); }
  
  @CheckForNull
  private VersionNumber getPluginVersion(@NonNull File dir, @NonNull String pluginId) {
    VersionNumber version = getPluginVersion(new File(dir, pluginId + ".jpi"));
    if (version == null)
      version = getPluginVersion(new File(dir, pluginId + ".hpi")); 
    return version;
  }
  
  @CheckForNull
  private VersionNumber getPluginVersion(@NonNull File pluginFile) {
    if (!pluginFile.exists())
      return null; 
    try {
      return getPluginVersion(pluginFile.toURI().toURL());
    } catch (MalformedURLException e) {
      return null;
    } 
  }
  
  @CheckForNull
  private VersionNumber getPluginVersion(@NonNull URL pluginURL) {
    Manifest manifest = parsePluginManifest(pluginURL);
    if (manifest == null)
      return null; 
    String versionSpec = manifest.getMainAttributes().getValue("Plugin-Version");
    return new VersionNumber(versionSpec);
  }
  
  private boolean containsHpiJpi(Collection<String> bundledPlugins, String name) {
    return (bundledPlugins.contains(name.replaceAll("\\.hpi", ".jpi")) || bundledPlugins.contains(name.replaceAll("\\.jpi", ".hpi")));
  }
  
  @Deprecated
  @CheckForNull
  public Manifest getBundledPluginManifest(String shortName) { return null; }
  
  public void dynamicLoad(File arc) throws IOException, InterruptedException, RestartRequiredException { dynamicLoad(arc, false, null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void dynamicLoad(File arc, boolean removeExisting, @CheckForNull List<PluginWrapper> batch) throws IOException, InterruptedException, RestartRequiredException {
    ACLContext context = ACL.as2(ACL.SYSTEM2);
    try {
      String sn;
      LOGGER.log(Level.FINE, "Attempting to dynamic load {0}", arc);
      PluginWrapper p = null;
      try {
        sn = this.strategy.getShortName(arc);
      } catch (AbstractMethodError x) {
        LOGGER.log(Level.WARNING, "JENKINS-12753 fix not active: {0}", x.getMessage());
        p = this.strategy.createPluginWrapper(arc);
        sn = p.getShortName();
      } 
      PluginWrapper pw = getPlugin(sn);
      if (pw != null)
        if (removeExisting) {
          for (Iterator<PluginWrapper> i = this.plugins.iterator(); i.hasNext(); ) {
            pw = (PluginWrapper)i.next();
            if (sn.equals(pw.getShortName())) {
              i.remove();
              break;
            } 
          } 
        } else {
          throw new RestartRequiredException(Messages._PluginManager_PluginIsAlreadyInstalled_RestartRequired(sn));
        }  
      if (p == null)
        p = this.strategy.createPluginWrapper(arc); 
      if (p.supportsDynamicLoad() == YesNoMaybe.NO)
        throw new RestartRequiredException(Messages._PluginManager_PluginDoesntSupportDynamicLoad_RestartRequired(sn)); 
      this.plugins.add(p);
      if (p.isActive())
        this.activePlugins.add(p); 
      ((UberClassLoader)this.uberClassLoader).loaded.clear();
      CustomClassFilter.Contributed.load();
      try {
        p.resolvePluginDependencies();
        this.strategy.load(p);
        if (batch != null) {
          batch.add(p);
        } else {
          start(List.of(p));
        } 
      } catch (Exception e) {
        this.failedPlugins.add(new FailedPlugin(p, e));
        this.activePlugins.remove(p);
        this.plugins.remove(p);
        p.releaseClassLoader();
        throw new IOException("Failed to install " + sn + " plugin", e);
      } 
      LOGGER.log(Level.FINE, "Plugin {0}:{1} dynamically {2}", new Object[] { p.getShortName(), p.getVersion(), (batch != null) ? "loaded but not yet started" : "installed" });
      if (context != null)
        context.close(); 
    } catch (Throwable throwable) {
      if (context != null)
        try {
          context.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void start(List<PluginWrapper> plugins) throws Exception {
    ACLContext context = ACL.as2(ACL.SYSTEM2);
    try {
      Map<String, PluginWrapper> pluginsByName = (Map)plugins.stream().collect(Collectors.toMap(PluginWrapper::getShortName, p -> p));
      for (PluginWrapper depender : this.plugins) {
        if (plugins.contains(depender))
          continue; 
        for (PluginWrapper.Dependency d : depender.getOptionalDependencies()) {
          PluginWrapper dependee = (PluginWrapper)pluginsByName.get(d.shortName);
          if (dependee != null)
            getPluginStrategy().updateDependency(depender, dependee); 
        } 
      } 
      resolveDependentPlugins();
      try {
        Jenkins.get().refreshExtensions();
      } catch (ExtensionRefreshException e) {
        throw new IOException("Failed to refresh extensions after installing some plugins", e);
      } 
      for (PluginWrapper p : plugins)
        p.getPluginOrFail().postInitialize(); 
      Reactor r = new Reactor(new TaskBuilder[] { InitMilestone.ordering() });
      Set<ClassLoader> loaders = (Set)plugins.stream().map(p -> p.classLoader).collect(Collectors.toSet());
      r.addAll((new Object(this, this.uberClassLoader, loaders)).discoverTasks(r));
      (new InitReactorRunner()).run(r);
      if (context != null)
        context.close(); 
    } catch (Throwable throwable) {
      if (context != null)
        try {
          context.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void resolveDependentPlugins() {
    for (PluginWrapper plugin : this.plugins) {
      Set<String> optionalDependents = new HashSet<String>();
      Set<String> dependents = new HashSet<String>();
      for (PluginWrapper possibleDependent : this.plugins) {
        if (possibleDependent.getShortName().equals(plugin.getShortName()))
          continue; 
        if (possibleDependent.isDeleted())
          continue; 
        List<PluginWrapper.Dependency> dependencies = possibleDependent.getDependencies();
        for (PluginWrapper.Dependency dependency : dependencies) {
          if (dependency.shortName.equals(plugin.getShortName())) {
            dependents.add(possibleDependent.getShortName());
            if (dependency.optional)
              optionalDependents.add(possibleDependent.getShortName()); 
          } 
        } 
      } 
      plugin.setDependents(dependents);
      plugin.setOptionalDependents(optionalDependents);
    } 
  }
  
  protected void copyBundledPlugin(URL src, String fileName) throws IOException {
    LOGGER.log(Level.FINE, "Copying {0}", src);
    fileName = fileName.replace(".hpi", ".jpi");
    String legacyName = fileName.replace(".jpi", ".hpi");
    long lastModified = getModificationDate(src);
    File file = new File(this.rootDir, fileName);
    rename(new File(this.rootDir, legacyName), file);
    if (!file.exists() || file.lastModified() != lastModified) {
      FileUtils.copyURLToFile(src, file);
      Files.setLastModifiedTime(Util.fileToPath(file), FileTime.fromMillis(getModificationDate(src)));
    } 
  }
  
  @CheckForNull
  static Manifest parsePluginManifest(URL bundledJpi) {
    try {
      cl = new URLClassLoader(new URL[] { bundledJpi });
      try {
        in = null;
        try {
          URL res = cl.findResource("META-INF/MANIFEST.MF");
          if (res != null) {
            in = getBundledJpiManifestStream(res);
            manifest = new Manifest(in);
            Util.closeAndLogFailures(in, LOGGER, "META-INF/MANIFEST.MF", bundledJpi.toString());
            return manifest;
          } 
        } finally {
          Util.closeAndLogFailures(in, LOGGER, "META-INF/MANIFEST.MF", bundledJpi.toString());
        } 
        cl.close();
      } catch (Throwable throwable) {
        try {
          cl.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to parse manifest of " + bundledJpi, e);
    } 
    return null;
  }
  
  @NonNull
  static InputStream getBundledJpiManifestStream(@NonNull URL url) throws IOException {
    URLConnection uc = url.openConnection();
    InputStream in = null;
    if (uc instanceof JarURLConnection) {
      JarURLConnection jarURLConnection = (JarURLConnection)uc;
      String entryName = jarURLConnection.getEntryName();
      JarFile jarFile = jarURLConnection.getJarFile();
      try {
        JarEntry entry = (entryName != null && jarFile != null) ? jarFile.getJarEntry(entryName) : null;
        if (entry != null) {
          InputStream i = jarFile.getInputStream(entry);
          try {
            byte[] manifestBytes = i.readAllBytes();
            in = new ByteArrayInputStream(manifestBytes);
            if (i != null)
              i.close(); 
          } catch (Throwable throwable) {
            if (i != null)
              try {
                i.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
        } else {
          LOGGER.log(Level.WARNING, "Failed to locate the JAR file for {0}The default URLConnection stream access will be used, file descriptor may be leaked.", url);
        } 
        if (jarFile != null)
          jarFile.close(); 
      } catch (Throwable throwable) {
        if (jarFile != null)
          try {
            jarFile.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } 
    if (in == null)
      in = url.openStream(); 
    return in;
  }
  
  @NonNull
  static long getModificationDate(@NonNull URL url) throws IOException {
    URLConnection uc = url.openConnection();
    if (uc instanceof JarURLConnection) {
      JarURLConnection connection = (JarURLConnection)uc;
      URL jarURL = connection.getJarFileURL();
      if (jarURL.getProtocol().equals("file")) {
        String file = jarURL.getFile();
        return (new File(file)).lastModified();
      } 
      if (connection.getEntryName() != null)
        LOGGER.log(Level.WARNING, "Accessing modification date of {0} file, which is an entry in JAR file. The access protocol is not file:, falling back to the default logic (risk of file descriptor leak).", url); 
    } 
    return uc.getLastModified();
  }
  
  private void rename(File legacyFile, File newFile) throws IOException {
    if (!legacyFile.exists())
      return; 
    if (newFile.exists())
      Util.deleteFile(newFile); 
    if (!legacyFile.renameTo(newFile))
      LOGGER.warning("Failed to rename " + legacyFile + " to " + newFile); 
  }
  
  protected PluginStrategy createPluginStrategy() {
    String strategyName = SystemProperties.getString(PluginStrategy.class.getName());
    if (strategyName != null) {
      try {
        Class<?> klazz = getClass().getClassLoader().loadClass(strategyName);
        Object strategy = klazz.getConstructor(new Class[] { PluginManager.class }).newInstance(new Object[] { this });
        if (strategy instanceof PluginStrategy) {
          LOGGER.info("Plugin strategy: " + strategyName);
          return (PluginStrategy)strategy;
        } 
        LOGGER.warning("Plugin strategy (" + strategyName + ") is not an instance of hudson.PluginStrategy");
      } catch (ClassNotFoundException e) {
        LOGGER.warning("Plugin strategy class not found: " + strategyName);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Could not instantiate plugin strategy: " + strategyName + ". Falling back to ClassicPluginStrategy", e);
      } 
      LOGGER.info("Falling back to ClassicPluginStrategy");
    } 
    return new ClassicPluginStrategy(this);
  }
  
  public PluginStrategy getPluginStrategy() { return this.strategy; }
  
  public boolean isPluginUploaded() { return this.pluginUploaded; }
  
  @Exported
  public List<PluginWrapper> getPlugins() { return Collections.unmodifiableList(this.plugins); }
  
  public List<FailedPlugin> getFailedPlugins() { return this.failedPlugins; }
  
  @CheckForNull
  public PluginWrapper getPlugin(String shortName) {
    for (PluginWrapper p : getPlugins()) {
      if (p.getShortName().equals(shortName))
        return p; 
    } 
    return null;
  }
  
  @CheckForNull
  public PluginWrapper getPlugin(Class<? extends Plugin> pluginClazz) {
    for (PluginWrapper p : getPlugins()) {
      if (pluginClazz.isInstance(p.getPlugin()))
        return p; 
    } 
    return null;
  }
  
  public List<PluginWrapper> getPlugins(Class<? extends Plugin> pluginSuperclass) {
    List<PluginWrapper> result = new ArrayList<PluginWrapper>();
    for (PluginWrapper p : getPlugins()) {
      if (pluginSuperclass.isInstance(p.getPlugin()))
        result.add(p); 
    } 
    return Collections.unmodifiableList(result);
  }
  
  public String getDisplayName() { return Messages.PluginManager_DisplayName(); }
  
  public String getSearchUrl() { return "pluginManager"; }
  
  @Deprecated
  public <T> Collection<Class<? extends T>> discover(Class<T> spi) {
    Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
    for (PluginWrapper p : this.activePlugins)
      Service.load(spi, p.classLoader, result); 
    return result;
  }
  
  public PluginWrapper whichPlugin(Class c) {
    PluginWrapper oneAndOnly = null;
    ClassLoader cl = c.getClassLoader();
    for (PluginWrapper p : this.activePlugins) {
      if (p.classLoader == cl) {
        if (oneAndOnly != null)
          return null; 
        oneAndOnly = p;
      } 
    } 
    if (oneAndOnly == null && Main.isUnitTest) {
      CodeSource cs = c.getProtectionDomain().getCodeSource();
      if (cs != null) {
        URL loc = cs.getLocation();
        if (loc != null && "file".equals(loc.getProtocol())) {
          File file;
          try {
            file = Paths.get(loc.toURI()).toFile();
          } catch (InvalidPathException|URISyntaxException e) {
            LOGGER.log(Level.WARNING, "could not inspect " + loc, e);
            return null;
          } 
          if (file.isFile())
            try {
              JarFile jf = new JarFile(file);
              try {
                Manifest mf = jf.getManifest();
                if (mf != null) {
                  Attributes attr = mf.getMainAttributes();
                  if (attr.getValue("Plugin-Version") != null) {
                    String shortName = attr.getValue("Short-Name");
                    LOGGER.fine(() -> "found " + shortName + " for " + c);
                    PluginWrapper pluginWrapper = getPlugin(shortName);
                    jf.close();
                    return pluginWrapper;
                  } 
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
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, "could not inspect " + loc, e);
            }  
        } 
      } 
    } 
    return oneAndOnly;
  }
  
  public void stop() {
    for (PluginWrapper p : this.activePlugins)
      p.stop(); 
    List<PluginWrapper> pluginsCopy = new ArrayList<PluginWrapper>(this.plugins);
    for (PluginWrapper p : pluginsCopy) {
      this.activePlugins.remove(p);
      this.plugins.remove(p);
      p.releaseClassLoader();
    } 
    LogFactory.release(this.uberClassLoader);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isNonMetaLabel(String label) { return (!"adopt-this-plugin".equals(label) && !"deprecated".equals(label)); }
  
  public UpdateCenterProxy getUpdates() { return new UpdateCenterProxy(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse doPluginsSearch(@QueryParameter String query, @QueryParameter Integer limit) {
    List<JSONObject> plugins = new ArrayList<JSONObject>();
    for (Iterator iterator = Jenkins.get().getUpdateCenter().getSiteList().iterator(); iterator.hasNext(); ) {
      UpdateSite site = (UpdateSite)iterator.next();
      List<JSONObject> sitePlugins = (List)site.getAvailables().stream().filter(plugin -> {
            if (StringUtils.isBlank(query))
              return true; 
            return (StringUtils.containsIgnoreCase(plugin.name, query) || StringUtils.containsIgnoreCase(plugin.title, query) || StringUtils.containsIgnoreCase(plugin.excerpt, query) || plugin.hasCategory(query) || plugin.getCategoriesStream().map(UpdateCenter::getCategoryDisplayName).anyMatch(()) || (plugin.hasWarnings() && query.equalsIgnoreCase("warning:")));
          }).limit(Math.max(limit.intValue() - plugins.size(), 1)).sorted((o1, o2) -> {
            String o1DisplayName = o1.getDisplayName();
            if (o1.name.equalsIgnoreCase(query) || o1DisplayName.equalsIgnoreCase(query))
              return -1; 
            String o2DisplayName = o2.getDisplayName();
            if (o2.name.equalsIgnoreCase(query) || o2DisplayName.equalsIgnoreCase(query))
              return 1; 
            if (o1.name.equals(o2.name))
              return 0; 
            int pop = Double.compare(o2.popularity.doubleValue(), o1.popularity.doubleValue());
            if (pop != 0)
              return pop; 
            return o1DisplayName.compareTo(o2DisplayName);
          }).map(plugin -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", plugin.name);
            jsonObject.put("sourceId", plugin.sourceId);
            jsonObject.put("title", plugin.title);
            jsonObject.put("displayName", plugin.getDisplayName());
            if (plugin.wiki == null || (!plugin.wiki.startsWith("https://") && !plugin.wiki.startsWith("http://"))) {
              jsonObject.put("wiki", "");
            } else {
              jsonObject.put("wiki", plugin.wiki);
            } 
            jsonObject.put("categories", plugin.getCategoriesStream().filter(PluginManager::isNonMetaLabel).map(UpdateCenter::getCategoryDisplayName).collect(Collectors.toList()));
            if (hasAdoptThisPluginLabel(plugin))
              jsonObject.put("adoptMe", Messages.PluginManager_adoptThisPlugin()); 
            if (plugin.isDeprecated())
              jsonObject.put("deprecated", Messages.PluginManager_deprecationWarning((plugin.getDeprecation()).url)); 
            jsonObject.put("excerpt", plugin.excerpt);
            jsonObject.put("version", plugin.version);
            jsonObject.put("popularity", plugin.popularity);
            if (plugin.isForNewerHudson())
              jsonObject.put("newerCoreRequired", Messages.PluginManager_coreWarning(Util.xmlEscape(plugin.requiredCore))); 
            if (plugin.hasWarnings()) {
              JSONObject unresolvedSecurityWarnings = new JSONObject();
              unresolvedSecurityWarnings.put("text", Messages.PluginManager_securityWarning());
              Set<UpdateSite.Warning> pluginWarnings = plugin.getWarnings();
              if (pluginWarnings == null)
                throw new IllegalStateException("warnings cannot be null here"); 
              List<JSONObject> warnings = (List)pluginWarnings.stream().map(()).collect(Collectors.toList());
              unresolvedSecurityWarnings.put("warnings", warnings);
              jsonObject.put("unresolvedSecurityWarnings", unresolvedSecurityWarnings);
            } 
            if (plugin.releaseTimestamp != null) {
              JSONObject releaseTimestamp = new JSONObject();
              releaseTimestamp.put("iso8601", Functions.iso8601DateTime(plugin.releaseTimestamp));
              releaseTimestamp.put("displayValue", Messages.PluginManager_ago(Functions.getTimeSpanString(plugin.releaseTimestamp)));
              jsonObject.put("releaseTimestamp", releaseTimestamp);
            } 
            return jsonObject;
          }).collect(Collectors.toList());
      plugins.addAll(sitePlugins);
      if (plugins.size() >= limit.intValue())
        break; 
    } 
    JSONArray mappedPlugins = new JSONArray();
    mappedPlugins.addAll(plugins);
    return HttpResponses.okJSON(mappedPlugins);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doPlugins() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    JSONArray response = new JSONArray();
    Map<String, JSONObject> allPlugins = new HashMap<String, JSONObject>();
    for (PluginWrapper plugin : this.plugins) {
      JSONObject pluginInfo = new JSONObject();
      pluginInfo.put("installed", Boolean.valueOf(true));
      pluginInfo.put("name", plugin.getShortName());
      pluginInfo.put("title", plugin.getDisplayName());
      pluginInfo.put("active", Boolean.valueOf(plugin.isActive()));
      pluginInfo.put("enabled", Boolean.valueOf(plugin.isEnabled()));
      pluginInfo.put("bundled", Boolean.valueOf(plugin.isBundled));
      pluginInfo.put("deleted", Boolean.valueOf(plugin.isDeleted()));
      pluginInfo.put("downgradable", Boolean.valueOf(plugin.isDowngradable()));
      pluginInfo.put("website", plugin.getUrl());
      List<PluginWrapper.Dependency> dependencies = plugin.getDependencies();
      if (dependencies != null && !dependencies.isEmpty()) {
        Map<String, String> dependencyMap = new HashMap<String, String>();
        for (PluginWrapper.Dependency dependency : dependencies)
          dependencyMap.put(dependency.shortName, dependency.version); 
        pluginInfo.put("dependencies", dependencyMap);
      } else {
        pluginInfo.put("dependencies", Collections.emptyMap());
      } 
      response.add(pluginInfo);
    } 
    for (UpdateSite site : Jenkins.get().getUpdateCenter().getSiteList()) {
      for (UpdateSite.Plugin plugin : site.getAvailables()) {
        JSONObject pluginInfo = (JSONObject)allPlugins.get(plugin.name);
        if (pluginInfo == null) {
          pluginInfo = new JSONObject();
          pluginInfo.put("installed", Boolean.valueOf(false));
        } 
        pluginInfo.put("name", plugin.name);
        pluginInfo.put("title", plugin.getDisplayName());
        pluginInfo.put("excerpt", plugin.excerpt);
        pluginInfo.put("site", site.getId());
        pluginInfo.put("dependencies", plugin.dependencies);
        pluginInfo.put("website", plugin.wiki);
        response.add(pluginInfo);
      } 
    } 
    return HttpResponses.okJSON(response);
  }
  
  @RequirePOST
  public HttpResponse doUpdateSources(StaplerRequest req) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (req.hasParameter("remove")) {
      UpdateCenter uc = Jenkins.get().getUpdateCenter();
      bc = new BulkChange(uc);
      try {
        for (String id : req.getParameterValues("sources"))
          uc.getSites().remove(uc.getById(id)); 
      } finally {
        bc.commit();
      } 
    } else if (req.hasParameter("add")) {
      return new HttpRedirect("addSite");
    } 
    return new HttpRedirect("./sites");
  }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void doInstallPluginsDone() {
    Jenkins j = Jenkins.get();
    j.checkPermission(Jenkins.ADMINISTER);
    InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_PLUGINS_INSTALLING);
  }
  
  @RequirePOST
  public void doInstall(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Set<String> plugins = new LinkedHashSet<String>();
    Enumeration<String> en = req.getParameterNames();
    while (en.hasMoreElements()) {
      String n = (String)en.nextElement();
      if (n.startsWith("plugin.")) {
        n = n.substring(7);
        plugins.add(n);
      } 
    } 
    boolean dynamicLoad = (req.getParameter("dynamicLoad") != null);
    install(plugins, dynamicLoad);
    rsp.sendRedirect("updates/");
  }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doInstallPlugins(StaplerRequest req) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    String payload = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
    JSONObject request = JSONObject.fromObject(payload);
    JSONArray pluginListJSON = request.getJSONArray("plugins");
    List<String> plugins = new ArrayList<String>();
    for (int i = 0; i < pluginListJSON.size(); i++)
      plugins.add(pluginListJSON.getString(i)); 
    UUID correlationId = UUID.randomUUID();
    try {
      boolean dynamicLoad = request.getBoolean("dynamicLoad");
      install(plugins, dynamicLoad, correlationId);
      JSONObject responseData = new JSONObject();
      responseData.put("correlationId", correlationId.toString());
      return HttpResponses.okJSON(responseData);
    } catch (RuntimeException e) {
      return HttpResponses.errorJSON(e.getMessage());
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<Future<UpdateCenter.UpdateCenterJob>> install(@NonNull Collection<String> plugins, boolean dynamicLoad) { return install(plugins, dynamicLoad, null); }
  
  private List<Future<UpdateCenter.UpdateCenterJob>> install(@NonNull Collection<String> plugins, boolean dynamicLoad, @CheckForNull UUID correlationId) {
    List<Future<UpdateCenter.UpdateCenterJob>> installJobs = new ArrayList<Future<UpdateCenter.UpdateCenterJob>>();
    LOGGER.log(Level.INFO, "Starting installation of a batch of {0} plugins plus their dependencies", Integer.valueOf(plugins.size()));
    long start = System.nanoTime();
    List<PluginWrapper> batch = new ArrayList<PluginWrapper>();
    for (String n : plugins) {
      int index = n.indexOf('.');
      UpdateSite.Plugin p = null;
      if (index == -1) {
        p = getPlugin(n, UpdateCenter.ID_DEFAULT);
      } else {
        while (index != -1 && index + 1 < n.length()) {
          String pluginName = n.substring(0, index);
          String siteName = n.substring(index + 1);
          UpdateSite.Plugin plugin = getPlugin(pluginName, siteName);
          if (plugin != null) {
            if (p != null)
              throw new Failure("Ambiguous plugin: " + n); 
            p = plugin;
          } 
          index = n.indexOf('.', index + 1);
        } 
      } 
      if (p == null)
        throw new Failure("No such plugin: " + n); 
      Future<UpdateCenter.UpdateCenterJob> jobFuture = p.deploy(dynamicLoad, correlationId, batch, false);
      installJobs.add(jobFuture);
    } 
    Jenkins jenkins = Jenkins.get();
    UpdateCenter updateCenter = jenkins.getUpdateCenter();
    if (dynamicLoad) {
      Objects.requireNonNull(updateCenter);
      installJobs.add(updateCenter.addJob(new UpdateCenter.CompleteBatchJob(updateCenter, batch, start, correlationId)));
    } 
    Authentication currentAuth = Jenkins.getAuthentication2();
    if (!jenkins.getInstallState().isSetupComplete()) {
      jenkins.setInstallState(InstallState.INITIAL_PLUGINS_INSTALLING);
      updateCenter.persistInstallStatus();
      (new Object(this, updateCenter, installJobs, currentAuth)).start();
    } 
    return installJobs;
  }
  
  @CheckForNull
  private UpdateSite.Plugin getPlugin(String pluginName, String siteName) {
    UpdateSite updateSite = Jenkins.get().getUpdateCenter().getById(siteName);
    if (updateSite == null)
      throw new Failure("No such update center: " + siteName); 
    return updateSite.getPlugin(pluginName);
  }
  
  @RequirePOST
  public HttpResponse doSiteConfigure(@QueryParameter String site) throws IOException {
    Jenkins hudson = Jenkins.get();
    hudson.checkPermission(Jenkins.ADMINISTER);
    UpdateCenter uc = hudson.getUpdateCenter();
    PersistedList<UpdateSite> sites = uc.getSites();
    sites.removeIf(s -> s.getId().equals(UpdateCenter.ID_DEFAULT));
    sites.add(new UpdateSite(UpdateCenter.ID_DEFAULT, site));
    return new HttpRedirect("advanced");
  }
  
  @POST
  public HttpResponse doProxyConfigure(StaplerRequest req) throws IOException {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.ADMINISTER);
    ProxyConfiguration pc = (ProxyConfiguration)req.bindJSON(ProxyConfiguration.class, req.getSubmittedForm());
    if (pc.name == null) {
      jenkins.proxy = null;
      ProxyConfiguration.getXmlFile().delete();
    } else {
      jenkins.proxy = pc;
      jenkins.proxy.save();
    } 
    return new HttpRedirect("advanced");
  }
  
  @RequirePOST
  public HttpResponse doUploadPlugin(StaplerRequest req) throws IOException {
    try {
      FileUploadPluginCopier fileUploadPluginCopier;
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      String fileName = "";
      File tmpDir = Files.createTempDirectory("uploadDir", new java.nio.file.attribute.FileAttribute[0]).toFile();
      ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(10240, tmpDir));
      List<FileItem> items = upload.parseRequest(req);
      if (StringUtils.isNotBlank(((FileItem)items.get(1)).getString())) {
        fileName = ((FileItem)items.get(1)).getString();
        fileUploadPluginCopier = new UrlPluginCopier(fileName);
      } else {
        FileItem fileItem = (FileItem)items.get(0);
        fileName = Util.getFileName(fileItem.getName());
        fileUploadPluginCopier = new FileUploadPluginCopier(fileItem);
      } 
      if ("".equals(fileName))
        return new HttpRedirect("advanced"); 
      if (!fileName.endsWith(".jpi") && !fileName.endsWith(".hpi"))
        throw new Failure(Messages.Hudson_NotAPlugin(fileName)); 
      File t = File.createTempFile("uploaded", ".jpi", tmpDir);
      tmpDir.deleteOnExit();
      t.deleteOnExit();
      Files.delete(Util.fileToPath(t));
      try {
        fileUploadPluginCopier.copy(t);
      } catch (Exception e) {
        throw new ServletException(e);
      } 
      fileUploadPluginCopier.cleanup();
      String baseName = identifyPluginShortName(t);
      this.pluginUploaded = true;
      JSONArray dependencies = new JSONArray();
      try {
        Manifest m;
        JarFile jarFile = new JarFile(t);
        try {
          m = jarFile.getManifest();
          jarFile.close();
        } catch (Throwable throwable) {
          try {
            jarFile.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        String deps = m.getMainAttributes().getValue("Plugin-Dependencies");
        if (StringUtils.isNotBlank(deps)) {
          String[] plugins = deps.split(",");
          for (String p : plugins) {
            String[] attrs = p.split("[:;]");
            dependencies.add((new JSONObject()).element("name", attrs[0]).element("version", attrs[1]).element("optional", p.contains("resolution:=optional")));
          } 
        } 
      } catch (IOException e) {
        Manifest m;
        LOGGER.log(Level.WARNING, "Unable to setup dependency list for plugin upload", m);
      } 
      JSONObject cfg = (new JSONObject()).element("name", baseName).element("version", "0").element("url", t.toURI().toString()).element("dependencies", dependencies);
      (new UpdateSite.Plugin(new UpdateSite("_upload", null), "_upload", cfg)).deploy(true);
      return new HttpRedirect("updates/");
    } catch (FileUploadException e) {
      throw new ServletException(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RequirePOST
  public FormValidation doCheckPluginUrl(StaplerRequest request, @QueryParameter String value) throws IOException {
    if (StringUtils.isNotBlank(value))
      try {
        URL url = new URL(value);
        if (!url.getProtocol().startsWith("http"))
          return FormValidation.error(Messages.PluginManager_invalidUrl()); 
        if (!url.getProtocol().equals("https"))
          return FormValidation.warning(Messages.PluginManager_insecureUrl()); 
      } catch (MalformedURLException e) {
        return FormValidation.error(e.getMessage());
      }  
    return FormValidation.ok();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RequirePOST
  public FormValidation doCheckUpdateSiteUrl(StaplerRequest request, @QueryParameter String value) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    return checkUpdateSiteURL(value);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  FormValidation checkUpdateSiteURL(@CheckForNull String value) throws InterruptedException {
    URI baseUri;
    value = Util.fixEmptyAndTrim(value);
    if (value == null)
      return FormValidation.error(Messages.PluginManager_emptyUpdateSiteUrl()); 
    try {
      baseUri = new URI(value);
    } catch (URISyntaxException ex) {
      return FormValidation.error(ex, Messages.PluginManager_invalidUrl());
    } 
    if ("file".equalsIgnoreCase(baseUri.getScheme())) {
      File f = new File(baseUri);
      if (f.isFile())
        return FormValidation.ok(); 
      return FormValidation.error(Messages.PluginManager_connectionFailed());
    } 
    if ("https".equalsIgnoreCase(baseUri.getScheme()) || "http".equalsIgnoreCase(baseUri.getScheme())) {
      HttpRequest httpRequest;
      URI uriWithQuery;
      try {
        if (baseUri.getRawQuery() == null) {
          uriWithQuery = new URI(value + "?version=" + value + "&uctest");
        } else {
          uriWithQuery = new URI(value + "&version=" + value + "&uctest");
        } 
      } catch (URISyntaxException e) {
        return FormValidation.error(e, Messages.PluginManager_invalidUrl());
      } 
      HttpClient httpClient = ProxyConfiguration.newHttpClientBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
      try {
        httpRequest = ProxyConfiguration.newHttpRequestBuilder(uriWithQuery).method("HEAD", HttpRequest.BodyPublishers.noBody()).build();
      } catch (IllegalArgumentException e) {
        return FormValidation.error(e, Messages.PluginManager_invalidUrl());
      } 
      try {
        HttpResponse<Void> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
        if (100 <= httpResponse.statusCode() && httpResponse.statusCode() <= 399)
          return FormValidation.ok(); 
        LOGGER.log(Level.FINE, "Obtained a non OK ({0}) response from the update center", new Object[] { Integer.valueOf(httpResponse.statusCode()), baseUri });
        return FormValidation.error(Messages.PluginManager_connectionFailed());
      } catch (IOException e) {
        LOGGER.log(Level.FINE, "Failed to check update site", e);
        return FormValidation.error(e, Messages.PluginManager_connectionFailed());
      } 
    } 
    return FormValidation.error(Messages.PluginManager_invalidUrl());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RequirePOST
  public HttpResponse doCheckUpdatesServer() {
    Jenkins.get().checkPermission(Jenkins.SYSTEM_READ);
    Retrier<FormValidation> updateServerRetrier = (new Retrier.Builder(this::checkUpdatesServer, (currentAttempt, result) -> (result.kind == FormValidation.Kind.OK), "check updates server")).withAttempts(CHECK_UPDATE_ATTEMPTS).withDelay(CHECK_UPDATE_SLEEP_TIME_MILLIS).withDuringActionExceptions(new Class[] { Exception.class }).withDuringActionExceptionListener((attempt, e) -> FormValidation.errorWithMarkup(e.getClass().getSimpleName() + ": " + e.getClass().getSimpleName())).build();
    try {
      FormValidation result = (FormValidation)updateServerRetrier.start();
      if (!FormValidation.Kind.OK.equals(result.kind)) {
        LOGGER.log(Level.SEVERE, Messages.PluginManager_UpdateSiteError(Integer.valueOf(CHECK_UPDATE_ATTEMPTS), result.getMessage()));
        if (CHECK_UPDATE_ATTEMPTS > 1 && !Logger.getLogger(Retrier.class.getName()).isLoggable(Level.WARNING))
          LOGGER.log(Level.SEVERE, Messages.PluginManager_UpdateSiteChangeLogLevel(Retrier.class.getName())); 
        this.lastErrorCheckUpdateCenters = Messages.PluginManager_CheckUpdateServerError(result.getMessage());
      } else {
        this.lastErrorCheckUpdateCenters = null;
      } 
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, Messages.PluginManager_UnexpectedException(), e);
      throw new IOException(e);
    } 
    return HttpResponses.forwardToPreviousPage();
  }
  
  private FormValidation checkUpdatesServer() throws Exception {
    for (UpdateSite site : Jenkins.get().getUpdateCenter().getSites()) {
      FormValidation v = site.updateDirectlyNow();
      if (v.kind != FormValidation.Kind.OK)
        return v; 
    } 
    for (DownloadService.Downloadable d : DownloadService.Downloadable.all()) {
      FormValidation v = d.updateNow();
      if (v.kind != FormValidation.Kind.OK)
        return v; 
    } 
    return FormValidation.ok();
  }
  
  public String getLastErrorCheckUpdateCenters() { return this.lastErrorCheckUpdateCenters; }
  
  protected String identifyPluginShortName(File t) {
    try {
      JarFile j = new JarFile(t);
      try {
        String name = j.getManifest().getMainAttributes().getValue("Short-Name");
        if (name != null) {
          String str = name;
          j.close();
          return str;
        } 
        j.close();
      } catch (Throwable throwable) {
        try {
          j.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to identify the short name from " + t, e);
    } 
    return FilenameUtils.getBaseName(t.getName());
  }
  
  public Descriptor<ProxyConfiguration> getProxyDescriptor() { return Jenkins.get().getDescriptor(ProxyConfiguration.class); }
  
  public List<Future<UpdateCenter.UpdateCenterJob>> prevalidateConfig(InputStream configXml) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    List<Future<UpdateCenter.UpdateCenterJob>> jobs = new ArrayList<Future<UpdateCenter.UpdateCenterJob>>();
    UpdateCenter uc = Jenkins.get().getUpdateCenter();
    for (Map.Entry<String, VersionNumber> requestedPlugin : parseRequestedPlugins(configXml).entrySet()) {
      PluginWrapper pw = getPlugin((String)requestedPlugin.getKey());
      if (pw == null) {
        UpdateSite.Plugin toInstall = uc.getPlugin((String)requestedPlugin.getKey(), (VersionNumber)requestedPlugin.getValue());
        if (toInstall == null) {
          LOGGER.log(Level.WARNING, "No such plugin {0} to install", requestedPlugin.getKey());
          continue;
        } 
        logPluginWarnings(requestedPlugin, toInstall);
        jobs.add(toInstall.deploy(true));
        continue;
      } 
      if (pw.isOlderThan((VersionNumber)requestedPlugin.getValue())) {
        UpdateSite.Plugin toInstall = uc.getPlugin((String)requestedPlugin.getKey(), (VersionNumber)requestedPlugin.getValue());
        if (toInstall == null) {
          LOGGER.log(Level.WARNING, "No such plugin {0} to upgrade", requestedPlugin.getKey());
          continue;
        } 
        if (!pw.isOlderThan(new VersionNumber(toInstall.version))) {
          LOGGER.log(Level.WARNING, "{0}@{1} is no newer than what we already have", new Object[] { toInstall.name, toInstall.version });
          continue;
        } 
        logPluginWarnings(requestedPlugin, toInstall);
        if (!toInstall.isCompatibleWithInstalledVersion())
          LOGGER.log(Level.WARNING, "{0}@{1} is incompatible with the installed @{2}", new Object[] { toInstall.name, toInstall.version, pw.getVersion() }); 
        jobs.add(toInstall.deploy(true));
      } 
    } 
    return jobs;
  }
  
  private void logPluginWarnings(Map.Entry<String, VersionNumber> requestedPlugin, UpdateSite.Plugin toInstall) {
    if ((new VersionNumber(toInstall.version)).compareTo((VersionNumber)requestedPlugin.getValue()) < 0)
      LOGGER.log(Level.WARNING, "{0} can only be satisfied in @{1}", new Object[] { requestedPlugin, toInstall.version }); 
    if (toInstall.isForNewerHudson())
      LOGGER.log(Level.WARNING, "{0}@{1} was built for a newer Jenkins", new Object[] { toInstall.name, toInstall.version }); 
  }
  
  @RequirePOST
  public JSONArray doPrevalidateConfig(StaplerRequest req) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    JSONArray response = new JSONArray();
    for (Map.Entry<String, VersionNumber> p : parseRequestedPlugins(req.getInputStream()).entrySet()) {
      PluginWrapper pw = getPlugin((String)p.getKey());
      JSONObject j = (new JSONObject()).accumulate("name", p.getKey()).accumulate("version", ((VersionNumber)p.getValue()).toString());
      if (pw == null) {
        response.add(j.accumulate("mode", "missing"));
        continue;
      } 
      if (pw.isOlderThan((VersionNumber)p.getValue()))
        response.add(j.accumulate("mode", "old")); 
    } 
    return response;
  }
  
  @RequirePOST
  public HttpResponse doInstallNecessaryPlugins(StaplerRequest req) throws IOException {
    prevalidateConfig(req.getInputStream());
    return HttpResponses.redirectViaContextPath("pluginManager/updates/");
  }
  
  public Map<String, VersionNumber> parseRequestedPlugins(InputStream configXml) throws IOException {
    Map<String, VersionNumber> requestedPlugins = new TreeMap<String, VersionNumber>();
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      spf.newSAXParser().parse(configXml, new Object(this, requestedPlugins));
    } catch (SAXException x) {
      throw new IOException("Failed to parse XML", x);
    } catch (ParserConfigurationException e) {
      throw new AssertionError(e);
    } 
    return requestedPlugins;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public MetadataCache createCache() { return new MetadataCache(); }
  
  @NonNull
  public List<PluginWrapper.PluginDisableResult> disablePlugins(@NonNull PluginWrapper.PluginDisableStrategy strategy, @NonNull List<String> plugins) throws IOException {
    List<PluginWrapper.PluginDisableResult> results = new ArrayList<PluginWrapper.PluginDisableResult>(plugins.size());
    for (String pluginName : plugins) {
      PluginWrapper plugin = getPlugin(pluginName);
      if (plugin == null) {
        results.add(new PluginWrapper.PluginDisableResult(pluginName, PluginWrapper.PluginDisableStatus.NO_SUCH_PLUGIN, Messages.PluginWrapper_NoSuchPlugin(pluginName)));
        continue;
      } 
      results.add(plugin.disable(strategy));
    } 
    return results;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public String unscientific(double d) { return String.format(Locale.US, "%15.4f", new Object[] { Double.valueOf(d) }); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.SYSTEM_READ); 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public boolean isMetaLabel(String label) { return ("adopt-this-plugin".equals(label) || "deprecated".equals(label)); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public boolean hasAdoptThisPluginLabel(UpdateSite.Plugin plugin) { return plugin.hasCategory("adopt-this-plugin"); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public boolean hasAdoptThisPluginLabel(PluginWrapper plugin) {
    UpdateSite.Plugin pluginMeta = Jenkins.get().getUpdateCenter().getPlugin(plugin.getShortName());
    if (pluginMeta == null)
      return false; 
    return pluginMeta.hasCategory("adopt-this-plugin");
  }
  
  protected abstract Collection<String> loadBundledPlugins() throws Exception;
}
