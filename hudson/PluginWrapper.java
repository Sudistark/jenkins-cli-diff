package hudson;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.UpdateCenter;
import hudson.model.UpdateSite;
import hudson.security.Permission;
import hudson.util.VersionNumber;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.YesNoMaybe;
import jenkins.model.Jenkins;
import jenkins.plugins.DetachedPluginsUtil;
import jenkins.security.UpdateSiteWarningsMonitor;
import jenkins.util.URLClassLoader2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
public class PluginWrapper extends Object implements Comparable<PluginWrapper>, ModelObject {
  private static final boolean ENABLE_PLUGIN_DEPENDENCIES_VERSION_CHECK = Boolean.parseBoolean(System.getProperty(PluginWrapper.class.getName() + ".dependenciesVersionCheck.enabled", "true"));
  
  public final PluginManager parent;
  
  private final Manifest manifest;
  
  public final ClassLoader classLoader;
  
  public final URL baseResourceURL;
  
  private final File disableFile;
  
  private final File archive;
  
  private final String shortName;
  
  private final boolean active;
  
  private boolean hasCycleDependency;
  
  private final List<Dependency> dependencies;
  
  private final List<Dependency> optionalDependencies;
  
  private final Map<String, Boolean> dependencyErrors;
  
  boolean isBundled;
  
  private Set<String> dependents;
  
  private Set<String> optionalDependents;
  
  public List<String> getDependencyErrors() { return List.copyOf(this.dependencyErrors.keySet()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<String> getOriginalDependencyErrors() {
    Predicate<Map.Entry<String, Boolean>> p = Map.Entry::getValue;
    return (List)this.dependencyErrors.entrySet().stream().filter(p.negate()).map(Map.Entry::getKey).collect(Collectors.toList());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasOriginalDependencyErrors() { return !getOriginalDependencyErrors().isEmpty(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<String> getDerivedDependencyErrors() { return (List)this.dependencyErrors.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasDerivedDependencyErrors() { return !getDerivedDependencyErrors().isEmpty(); }
  
  private static Set<String> CORE_ONLY_DEPENDANT = Set.of("jenkins-core");
  
  public void setDependents(@NonNull Set<String> dependents) { this.dependents = dependents; }
  
  @Deprecated
  public void setDependants(@NonNull Set<String> dependents) { setDependents(dependents); }
  
  public void setOptionalDependents(@NonNull Set<String> optionalDependents) { this.optionalDependents = optionalDependents; }
  
  @Deprecated
  public void setOptionalDependants(@NonNull Set<String> optionalDependents) { setOptionalDependents(this.dependents); }
  
  @NonNull
  public Set<String> getDependents() {
    if (this.isBundled && this.dependents.isEmpty())
      return CORE_ONLY_DEPENDANT; 
    return this.dependents;
  }
  
  @Deprecated
  @NonNull
  public Set<String> getDependants() { return getDependents(); }
  
  @NonNull
  public Set<String> getMandatoryDependents() {
    Set<String> s = new HashSet<String>(this.dependents);
    s.removeAll(this.optionalDependents);
    return s;
  }
  
  @NonNull
  public Set<String> getOptionalDependents() { return this.optionalDependents; }
  
  @Deprecated
  @NonNull
  public Set<String> getOptionalDependants() { return getOptionalDependents(); }
  
  public boolean hasDependents() { return (this.isBundled || !this.dependents.isEmpty()); }
  
  public boolean hasMandatoryDependents() {
    if (this.isBundled)
      return true; 
    return this.dependents.stream().anyMatch(d -> !this.optionalDependents.contains(d));
  }
  
  @Deprecated
  public boolean hasDependants() { return hasDependents(); }
  
  public boolean hasOptionalDependents() { return !this.optionalDependents.isEmpty(); }
  
  @Deprecated
  public boolean hasOptionalDependants() { return hasOptionalDependents(); }
  
  public boolean hasDependencies() { return !this.dependencies.isEmpty(); }
  
  public boolean hasMandatoryDependencies() { return this.dependencies.stream().anyMatch(d -> !d.optional); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isDeprecated() { return !getDeprecations().isEmpty(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void injectJarsToClasspath(File... jars) throws Exception {
    if (this.classLoader instanceof URLClassLoader2) {
      for (File f : jars) {
        LOGGER.log(Level.CONFIG, () -> "Inserting " + f + " into " + this.shortName + " plugin's classpath");
        ((URLClassLoader2)this.classLoader).addURL(f.toURI().toURL());
      } 
    } else {
      throw new AssertionError("PluginWrapper classloader has changed type, but this code has not been updated accordingly");
    } 
  }
  
  public PluginWrapper(PluginManager parent, File archive, Manifest manifest, URL baseResourceURL, ClassLoader classLoader, File disableFile, List<Dependency> dependencies, List<Dependency> optionalDependencies) {
    this.hasCycleDependency = false;
    this.dependencyErrors = new HashMap(0);
    this.dependents = Collections.emptySet();
    this.optionalDependents = Collections.emptySet();
    this.parent = parent;
    this.manifest = manifest;
    this.shortName = Util.intern(computeShortName(manifest, archive.getName()));
    this.baseResourceURL = baseResourceURL;
    this.classLoader = classLoader;
    this.disableFile = disableFile;
    this.active = !disableFile.exists();
    this.dependencies = dependencies;
    this.optionalDependencies = optionalDependencies;
    for (Dependency d : optionalDependencies)
      assert d.optional : "" + d + " included among optionalDependencies of " + d + " but was not marked optional"; 
    this.archive = archive;
  }
  
  public String getDisplayName() { return StringUtils.removeStart(getLongName(), "Jenkins "); }
  
  public Api getApi() {
    Jenkins.get().checkAnyPermission(new Permission[] { Jenkins.SYSTEM_READ, Jenkins.MANAGE });
    return new Api(this);
  }
  
  public URL getIndexPage() {
    URL idx = null;
    try {
      Enumeration<URL> en = this.classLoader.getResources("index.jelly");
      while (en.hasMoreElements())
        idx = (URL)en.nextElement(); 
    } catch (IOException iOException) {}
    return (idx != null && idx.toString().contains(this.shortName)) ? idx : null;
  }
  
  static String computeShortName(Manifest manifest, String fileName) {
    String n = manifest.getMainAttributes().getValue("Short-Name");
    if (n != null)
      return n; 
    n = manifest.getMainAttributes().getValue("Extension-Name");
    if (n != null)
      return n; 
    return FilenameUtils.getBaseName(fileName);
  }
  
  @Exported
  public List<Dependency> getDependencies() { return this.dependencies; }
  
  public List<Dependency> getMandatoryDependencies() { return (List)this.dependencies.stream().filter(d -> !d.optional).collect(Collectors.toList()); }
  
  public List<Dependency> getOptionalDependencies() { return this.optionalDependencies; }
  
  @Exported
  public String getShortName() { return this.shortName; }
  
  @CheckForNull
  public Plugin getPlugin() {
    PluginManager.PluginInstanceStore pis = (PluginManager.PluginInstanceStore)Jenkins.lookup(PluginManager.PluginInstanceStore.class);
    return (pis != null) ? (Plugin)pis.store.get(this) : null;
  }
  
  @NonNull
  public Plugin getPluginOrFail() {
    Plugin plugin = getPlugin();
    if (plugin == null)
      throw new Exception("Cannot find the plugin instance: " + this.shortName); 
    return plugin;
  }
  
  @Exported
  public String getUrl() {
    List<UpdateSite.Plugin> siteMetadataList = getInfoFromAllSites();
    String firstSiteUrl = null;
    if (!siteMetadataList.isEmpty()) {
      firstSiteUrl = ((UpdateSite.Plugin)siteMetadataList.get(0)).wiki;
      if (allUrlsMatch(firstSiteUrl, siteMetadataList))
        return firstSiteUrl; 
    } 
    String url = this.manifest.getMainAttributes().getValue("Url");
    if (url != null)
      return url; 
    return firstSiteUrl;
  }
  
  private boolean allUrlsMatch(String url, List<UpdateSite.Plugin> uiList) { return uiList.stream().allMatch(k -> (k.wiki != null && k.wiki.equals(url))); }
  
  public String toString() { return "Plugin:" + getShortName(); }
  
  @Exported
  @Deprecated
  public String getLongName() {
    String name = this.manifest.getMainAttributes().getValue("Long-Name");
    if (name != null)
      return name; 
    return this.shortName;
  }
  
  @Exported
  public YesNoMaybe supportsDynamicLoad() {
    String v = this.manifest.getMainAttributes().getValue("Support-Dynamic-Loading");
    if (v == null)
      return YesNoMaybe.MAYBE; 
    return Boolean.parseBoolean(v) ? YesNoMaybe.YES : YesNoMaybe.NO;
  }
  
  @Exported
  public String getVersion() { return getVersionOf(this.manifest); }
  
  private String getVersionOf(Manifest manifest) {
    String v = manifest.getMainAttributes().getValue("Plugin-Version");
    if (v != null)
      return v; 
    v = manifest.getMainAttributes().getValue("Implementation-Version");
    if (v != null)
      return v; 
    return "???";
  }
  
  @Exported
  @CheckForNull
  public String getRequiredCoreVersion() {
    String v = this.manifest.getMainAttributes().getValue("Jenkins-Version");
    if (v != null)
      return v; 
    v = this.manifest.getMainAttributes().getValue("Hudson-Version");
    if (v != null)
      return v; 
    return null;
  }
  
  public VersionNumber getVersionNumber() { return new VersionNumber(getVersion()); }
  
  public boolean isOlderThan(VersionNumber v) {
    try {
      return (getVersionNumber().compareTo(v) < 0);
    } catch (IllegalArgumentException e) {
      return true;
    } 
  }
  
  public void stop() {
    Plugin plugin = getPlugin();
    if (plugin != null) {
      try {
        LOGGER.log(Level.FINE, "Stopping {0}", this.shortName);
        plugin.stop();
      } catch (Throwable t) {
        LOGGER.log(Level.WARNING, "Failed to shut down " + this.shortName, t);
      } 
    } else {
      LOGGER.log(Level.FINE, "Could not find Plugin instance to stop for {0}", this.shortName);
    } 
    LogFactory.release(this.classLoader);
  }
  
  public void releaseClassLoader() {
    if (this.classLoader instanceof Closeable)
      try {
        ((Closeable)this.classLoader).close();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to shut down classloader", e);
      }  
  }
  
  public void enable() {
    if (!this.disableFile.exists()) {
      LOGGER.log(Level.FINEST, "Plugin {0} has been already enabled. Skipping the enable() operation", getShortName());
      return;
    } 
    if (!this.disableFile.delete())
      throw new IOException("Failed to delete " + this.disableFile); 
  }
  
  @Deprecated
  public void disable() { disableWithoutCheck(); }
  
  private void disableWithoutCheck() {
    try {
      OutputStream os = Files.newOutputStream(this.disableFile.toPath(), new java.nio.file.OpenOption[0]);
      if (os != null)
        os.close(); 
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  @NonNull
  public PluginDisableResult disable(@NonNull PluginDisableStrategy strategy) {
    PluginDisableResult result = new PluginDisableResult(this.shortName);
    if (!isEnabled()) {
      result.setMessage(Messages.PluginWrapper_Already_Disabled(this.shortName));
      result.setStatus(PluginDisableStatus.ALREADY_DISABLED);
      return result;
    } 
    String aDependentNotDisabled = null;
    Set<String> dependentsToCheck = dependentsToCheck(strategy);
    for (String dependent : dependentsToCheck) {
      PluginWrapper dependentPlugin = this.parent.getPlugin(dependent);
      if (dependentPlugin == null) {
        PluginDisableResult dependentStatus = new PluginDisableResult(dependent, PluginDisableStatus.NO_SUCH_PLUGIN, Messages.PluginWrapper_NoSuchPlugin(dependent));
        result.addDependentDisableStatus(dependentStatus);
        continue;
      } 
      if (strategy.equals(PluginDisableStrategy.NONE)) {
        if (dependentPlugin.isEnabled()) {
          aDependentNotDisabled = dependent;
          break;
        } 
        continue;
      } 
      if (!dependentPlugin.isEnabled()) {
        PluginDisableResult dependentStatus = new PluginDisableResult(dependent, PluginDisableStatus.ALREADY_DISABLED, Messages.PluginWrapper_Already_Disabled(dependent));
        result.addDependentDisableStatus(dependentStatus);
        continue;
      } 
      PluginDisableResult dependentResult = dependentPlugin.disable(strategy);
      PluginDisableStatus dependentStatus = dependentResult.status;
      if (PluginDisableStatus.ERROR_DISABLING.equals(dependentStatus) || PluginDisableStatus.NOT_DISABLED_DEPENDANTS.equals(dependentStatus)) {
        aDependentNotDisabled = dependent;
        break;
      } 
      result.addDependentDisableStatus(dependentResult);
    } 
    if (aDependentNotDisabled == null) {
      try {
        disableWithoutCheck();
        result.setMessage(Messages.PluginWrapper_Plugin_Disabled(this.shortName));
        result.setStatus(PluginDisableStatus.DISABLED);
      } catch (IOException io) {
        result.setMessage(Messages.PluginWrapper_Error_Disabling(this.shortName, io.toString()));
        result.setStatus(PluginDisableStatus.ERROR_DISABLING);
      } 
    } else {
      result.setMessage(Messages.PluginWrapper_Plugin_Has_Dependent(this.shortName, aDependentNotDisabled, strategy));
      result.setStatus(PluginDisableStatus.NOT_DISABLED_DEPENDANTS);
    } 
    return result;
  }
  
  private Set<String> dependentsToCheck(PluginDisableStrategy strategy) {
    switch (null.$SwitchMap$hudson$PluginWrapper$PluginDisableStrategy[strategy.ordinal()]) {
      case 1:
        return getDependents();
    } 
    return Sets.difference(getDependents(), getOptionalDependents());
  }
  
  @Exported
  public boolean isActive() { return (this.active && !hasCycleDependency()); }
  
  public boolean hasCycleDependency() { return this.hasCycleDependency; }
  
  public void setHasCycleDependency(boolean hasCycle) { this.hasCycleDependency = hasCycle; }
  
  @Exported
  public boolean isBundled() { return this.isBundled; }
  
  @Exported
  public boolean isEnabled() { return !this.disableFile.exists(); }
  
  public Manifest getManifest() { return this.manifest; }
  
  public void setPlugin(Plugin plugin) {
    ((PluginManager.PluginInstanceStore)Jenkins.lookup(PluginManager.PluginInstanceStore.class)).store.put(this, plugin);
    plugin.wrapper = this;
  }
  
  public String getPluginClass() { return this.manifest.getMainAttributes().getValue("Plugin-Class"); }
  
  public boolean hasLicensesXml() {
    try {
      (new URL(this.baseResourceURL, "WEB-INF/licenses.xml")).openStream().close();
      return true;
    } catch (IOException e) {
      return false;
    } 
  }
  
  void resolvePluginDependencies() {
    if (ENABLE_PLUGIN_DEPENDENCIES_VERSION_CHECK) {
      String requiredCoreVersion = getRequiredCoreVersion();
      if (requiredCoreVersion == null) {
        LOGGER.warning(this.shortName + " doesn't declare required core version.");
      } else {
        VersionNumber actualVersion = Jenkins.getVersion();
        if (actualVersion.isOlderThan(new VersionNumber(requiredCoreVersion)))
          versionDependencyError(Messages.PluginWrapper_obsoleteCore(Jenkins.getVersion().toString(), requiredCoreVersion), Jenkins.getVersion().toString(), requiredCoreVersion); 
      } 
    } 
    for (Dependency d : this.dependencies) {
      PluginWrapper dependency = this.parent.getPlugin(d.shortName);
      if (dependency == null) {
        PluginWrapper failedDependency = NOTICE.getPlugin(d.shortName);
        if (failedDependency != null) {
          this.dependencyErrors.put(Messages.PluginWrapper_failed_to_load_dependency_2(failedDependency.getLongName(), failedDependency.getShortName(), failedDependency.getVersion()), Boolean.valueOf(true));
          break;
        } 
        this.dependencyErrors.put(Messages.PluginWrapper_missing(d.shortName, d.version), Boolean.valueOf(false));
        continue;
      } 
      if (dependency.isActive()) {
        if (isDependencyObsolete(d, dependency))
          versionDependencyError(Messages.PluginWrapper_obsolete_2(dependency.getLongName(), dependency.getShortName(), dependency.getVersion(), d.version), dependency.getVersion(), d.version); 
        continue;
      } 
      if (isDependencyObsolete(d, dependency)) {
        versionDependencyError(Messages.PluginWrapper_obsolete_2(dependency.getLongName(), dependency.getShortName(), dependency.getVersion(), d.version), dependency.getVersion(), d.version);
        continue;
      } 
      this.dependencyErrors.put(Messages.PluginWrapper_disabled_2(dependency.getLongName(), dependency.getShortName()), Boolean.valueOf(false));
    } 
    for (Dependency d : this.optionalDependencies) {
      PluginWrapper dependency = this.parent.getPlugin(d.shortName);
      if (dependency != null && dependency.isActive()) {
        if (isDependencyObsolete(d, dependency)) {
          versionDependencyError(Messages.PluginWrapper_obsolete_2(dependency.getLongName(), dependency.getShortName(), dependency.getVersion(), d.version), dependency.getVersion(), d.version);
          continue;
        } 
        this.dependencies.add(d);
      } 
    } 
    if (!this.dependencyErrors.isEmpty()) {
      NOTICE.addPlugin(this);
      StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(Messages.PluginWrapper_failed_to_load_plugin_2(getLongName(), getShortName(), getVersion())).append(System.lineSeparator());
      for (Iterator<String> iterator = this.dependencyErrors.keySet().iterator(); iterator.hasNext(); ) {
        String dependencyError = (String)iterator.next();
        messageBuilder.append(" - ").append(dependencyError);
        if (iterator.hasNext())
          messageBuilder.append(System.lineSeparator()); 
      } 
      throw new IOException(messageBuilder.toString());
    } 
  }
  
  private boolean isDependencyObsolete(Dependency d, PluginWrapper dependency) { return (ENABLE_PLUGIN_DEPENDENCIES_VERSION_CHECK && dependency.getVersionNumber().isOlderThan(new VersionNumber(d.version))); }
  
  private void versionDependencyError(String message, String actual, String minimum) {
    if (isSnapshot(actual) || isSnapshot(minimum)) {
      LOGGER.log(Level.WARNING, "Suppressing dependency error in {0} v{1}: {2}", new Object[] { getShortName(), getVersion(), message });
    } else {
      this.dependencyErrors.put(message, Boolean.valueOf(false));
    } 
  }
  
  static boolean isSnapshot(@NonNull String version) { return (version.contains("-SNAPSHOT") || version.matches(".+-[0-9]{8}.[0-9]{6}-[0-9]+")); }
  
  public UpdateSite.Plugin getUpdateInfo() {
    UpdateCenter uc = Jenkins.get().getUpdateCenter();
    UpdateSite.Plugin p = uc.getPlugin(getShortName(), getVersionNumber());
    if (p != null && p.isNewerThan(getVersion()))
      return p; 
    return null;
  }
  
  public UpdateSite.Plugin getInfo() {
    UpdateCenter uc = Jenkins.get().getUpdateCenter();
    UpdateSite.Plugin p = uc.getPlugin(getShortName(), getVersionNumber());
    if (p != null)
      return p; 
    return uc.getPlugin(getShortName());
  }
  
  private List<UpdateSite.Plugin> getInfoFromAllSites() {
    UpdateCenter uc = Jenkins.get().getUpdateCenter();
    return uc.getPluginFromAllSites(getShortName(), getVersionNumber());
  }
  
  @Exported
  public boolean hasUpdate() { return (getUpdateInfo() != null); }
  
  @Exported
  @Deprecated
  public boolean isPinned() { return false; }
  
  @Exported
  public boolean isDeleted() { return !this.archive.exists(); }
  
  @Exported
  public boolean isDetached() { return DetachedPluginsUtil.isDetachedPlugin(this.shortName); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasImpliedDependents() {
    if (!isDetached())
      return false; 
    for (PluginWrapper p : Jenkins.get().getPluginManager().getPlugins()) {
      for (Dependency dependency : DetachedPluginsUtil.getImpliedDependencies(p.shortName, p.getRequiredCoreVersion())) {
        if (dependency.shortName.equals(this.shortName))
          return true; 
      } 
    } 
    return false;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public Set<String> getImpliedDependents() {
    if (!isDetached())
      return Collections.emptySet(); 
    Set<String> implied = new HashSet<String>();
    for (PluginWrapper p : Jenkins.get().getPluginManager().getPlugins()) {
      for (Dependency dependency : DetachedPluginsUtil.getImpliedDependencies(p.shortName, p.getRequiredCoreVersion())) {
        if (dependency.shortName.equals(this.shortName))
          implied.add(p.shortName); 
      } 
    } 
    return implied;
  }
  
  public int compareTo(PluginWrapper pw) { return this.shortName.compareToIgnoreCase(pw.shortName); }
  
  @Exported
  public boolean isDowngradable() { return getBackupFile().exists(); }
  
  public File getBackupFile() {
    return new File(Jenkins.get().getRootDir(), "plugins/" + getShortName() + ".bak");
  }
  
  @Exported
  public String getBackupVersion() {
    File backup = getBackupFile();
    if (backup.exists())
      try {
        JarFile backupPlugin = new JarFile(backup);
        try {
          String str = backupPlugin.getManifest().getMainAttributes().getValue("Plugin-Version");
          backupPlugin.close();
          return str;
        } catch (Throwable throwable) {
          try {
            backupPlugin.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to get backup version from " + backup, e);
        return null;
      }  
    return null;
  }
  
  @Deprecated
  public boolean isPinningForcingOldVersion() { return false; }
  
  @Extension
  public static final PluginWrapperAdministrativeMonitor NOTICE = new PluginWrapperAdministrativeMonitor();
  
  @RequirePOST
  public HttpResponse doMakeEnabled() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    enable();
    return HttpResponses.ok();
  }
  
  @RequirePOST
  public HttpResponse doMakeDisabled() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    disable();
    return HttpResponses.ok();
  }
  
  @RequirePOST
  @Deprecated
  public HttpResponse doPin() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    LOGGER.log(Level.WARNING, "Call to pin plugin has been ignored. Plugin name: " + this.shortName);
    return HttpResponses.ok();
  }
  
  @RequirePOST
  @Deprecated
  public HttpResponse doUnpin() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    LOGGER.log(Level.WARNING, "Call to unpin plugin has been ignored. Plugin name: " + this.shortName);
    return HttpResponses.ok();
  }
  
  @RequirePOST
  public HttpResponse doDoUninstall() throws IOException {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.ADMINISTER);
    Files.deleteIfExists(Util.fileToPath(this.archive));
    Files.deleteIfExists(Util.fileToPath(this.disableFile));
    jenkins.getPluginManager().resolveDependentPlugins();
    return HttpResponses.redirectViaContextPath("/pluginManager/installed");
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public List<UpdateSite.Warning> getActiveWarnings() { return (List)((UpdateSiteWarningsMonitor)ExtensionList.lookupSingleton(UpdateSiteWarningsMonitor.class)).getActivePluginWarningsByPlugin().getOrDefault(this, Collections.emptyList()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<UpdateSite.Deprecation> getDeprecations() {
    List<UpdateSite.Deprecation> deprecations = new ArrayList<UpdateSite.Deprecation>();
    UpdateCenter updateCenter = Jenkins.get().getUpdateCenter();
    if (updateCenter.isSiteDataReady())
      for (UpdateSite site : updateCenter.getSites()) {
        UpdateSite.Data data = site.getData();
        if (data != null)
          for (Map.Entry<String, UpdateSite.Deprecation> entry : data.getDeprecations().entrySet()) {
            if (((String)entry.getKey()).equals(this.shortName))
              deprecations.add((UpdateSite.Deprecation)entry.getValue()); 
          }  
      }  
    return deprecations;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public String getIssueTrackerReportUrl() {
    UpdateCenter updateCenter = Jenkins.get().getUpdateCenter();
    if (updateCenter.isSiteDataReady())
      for (UpdateSite site : updateCenter.getSites()) {
        UpdateSite.Plugin sitePlugin = site.getPlugin(this.shortName);
        if (sitePlugin != null && sitePlugin.issueTrackers != null)
          for (UpdateSite.IssueTracker issueTracker : sitePlugin.issueTrackers) {
            if (issueTracker.reportUrl != null)
              return issueTracker.reportUrl; 
          }  
      }  
    return null;
  }
  
  private static final Logger LOGGER = Logger.getLogger(PluginWrapper.class.getName());
  
  public static final String MANIFEST_FILENAME = "META-INF/MANIFEST.MF";
}
