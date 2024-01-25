package hudson.model;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.PluginManager;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.lifecycle.Lifecycle;
import hudson.model.listeners.SaveableListener;
import hudson.remoting.AtmostOneThreadExecutor;
import hudson.util.DaemonThreadFactory;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.NamingThreadFactory;
import hudson.util.PersistedList;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.install.InstallUtil;
import jenkins.management.Badge;
import jenkins.management.Messages;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerDispatchable;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
public class UpdateCenter extends AbstractModelObject implements Saveable, OnMaster, StaplerProxy {
  private static final Logger LOGGER;
  
  private static final String UPDATE_CENTER_URL;
  
  private static final int PLUGIN_DOWNLOAD_READ_TIMEOUT = (int)TimeUnit.SECONDS.toMillis(SystemProperties.getInteger(UpdateCenter.class.getName() + ".pluginDownloadReadTimeoutSeconds", Integer.valueOf(60)).intValue());
  
  public static final String PREDEFINED_UPDATE_SITE_ID = "default";
  
  public static final String ID_DEFAULT = SystemProperties.getString(UpdateCenter.class.getName() + ".defaultUpdateSiteId", "default");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String ID_UPLOAD = "_upload";
  
  private final ExecutorService installerService;
  
  protected final ExecutorService updateService;
  
  private final Vector<UpdateCenterJob> jobs;
  
  private final Set<UpdateSite> sourcesUsed;
  
  private final PersistedList<UpdateSite> sites;
  
  private UpdateCenterConfiguration config;
  
  private boolean requiresRestart;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK;
  
  private static final AtomicInteger iota;
  
  @Deprecated
  public static boolean neverUpdate;
  
  public static final XStream2 XSTREAM;
  
  static  {
    logger = Logger.getLogger(UpdateCenter.class.getName());
    LOGGER = logger;
    String ucOverride = SystemProperties.getString(UpdateCenter.class.getName() + ".updateCenterUrl");
    if (ucOverride != null) {
      logger.log(Level.INFO, "Using a custom update center defined by the system property: {0}", ucOverride);
      UPDATE_CENTER_URL = ucOverride;
    } else {
      UPDATE_CENTER_URL = "https://updates.jenkins.io/";
    } 
    SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(UpdateCenter.class.getName() + ".skipPermissionCheck");
    iota = new AtomicInteger();
    neverUpdate = SystemProperties.getBoolean(UpdateCenter.class.getName() + ".never");
    XSTREAM = new XStream2();
    XSTREAM.alias("site", UpdateSite.class);
    XSTREAM.alias("sites", PersistedList.class);
  }
  
  public UpdateCenter() {
    this.installerService = new AtmostOneThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), "Update center installer thread"));
    this.updateService = Executors.newCachedThreadPool(new NamingThreadFactory(new DaemonThreadFactory(), "Update site data downloader"));
    this.jobs = new Vector();
    this.sourcesUsed = new HashSet();
    this.sites = new PersistedList(this);
    configure(new UpdateCenterConfiguration());
  }
  
  UpdateCenter(@NonNull UpdateCenterConfiguration configuration) {
    this.installerService = new AtmostOneThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), "Update center installer thread"));
    this.updateService = Executors.newCachedThreadPool(new NamingThreadFactory(new DaemonThreadFactory(), "Update site data downloader"));
    this.jobs = new Vector();
    this.sourcesUsed = new HashSet();
    this.sites = new PersistedList(this);
    configure(configuration);
  }
  
  @NonNull
  public static UpdateCenter createUpdateCenter(@CheckForNull UpdateCenterConfiguration config) {
    String requiredClassName = SystemProperties.getString(UpdateCenter.class.getName() + ".className", null);
    if (requiredClassName == null) {
      LOGGER.log(Level.FINE, "Using the default Update Center implementation");
      return createDefaultUpdateCenter(config);
    } 
    LOGGER.log(Level.FINE, "Using the custom update center: {0}", requiredClassName);
    try {
      Class<?> clazz = Class.forName(requiredClassName).asSubclass(UpdateCenter.class);
      if (!UpdateCenter.class.isAssignableFrom(clazz)) {
        LOGGER.log(Level.SEVERE, "The specified custom Update Center {0} is not an instance of {1}. Falling back to default.", new Object[] { requiredClassName, UpdateCenter.class.getName() });
        return createDefaultUpdateCenter(config);
      } 
      Class<? extends UpdateCenter> ucClazz = clazz.asSubclass(UpdateCenter.class);
      Constructor<? extends UpdateCenter> defaultConstructor = ucClazz.getConstructor(new Class[0]);
      Constructor<? extends UpdateCenter> configConstructor = ucClazz.getConstructor(new Class[] { UpdateCenterConfiguration.class });
      LOGGER.log(Level.FINE, "Using the constructor {0} Update Center configuration for {1}", new Object[] { (config != null) ? "with" : "without", requiredClassName });
      return (config != null) ? (UpdateCenter)configConstructor.newInstance(new Object[] { config }) : (UpdateCenter)defaultConstructor.newInstance(new Object[0]);
    } catch (ClassCastException e) {
      LOGGER.log(Level.WARNING, "UpdateCenter class {0} does not extend hudson.model.UpdateCenter. Using default.", requiredClassName);
    } catch (NoSuchMethodException e) {
      LOGGER.log(Level.WARNING, String.format("UpdateCenter class %s does not define one of the required constructors. Using default", new Object[] { requiredClassName }), e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, String.format("Unable to instantiate custom plugin manager [%s]. Using default.", new Object[] { requiredClassName }), e);
    } 
    return createDefaultUpdateCenter(config);
  }
  
  @NonNull
  private static UpdateCenter createDefaultUpdateCenter(@CheckForNull UpdateCenterConfiguration config) { return (config != null) ? new UpdateCenter(config) : new UpdateCenter(); }
  
  public Api getApi() { return new Api(this); }
  
  public void configure(UpdateCenterConfiguration config) {
    if (config != null)
      this.config = config; 
  }
  
  @Exported
  @StaplerDispatchable
  public List<UpdateCenterJob> getJobs() {
    synchronized (this.jobs) {
      return new ArrayList(this.jobs);
    } 
  }
  
  public UpdateCenterJob getJob(int id) {
    synchronized (this.jobs) {
      for (UpdateCenterJob job : this.jobs) {
        if (job.id == id)
          return job; 
      } 
    } 
    return null;
  }
  
  public InstallationJob getJob(UpdateSite.Plugin plugin) {
    List<UpdateCenterJob> jobList = getJobs();
    Collections.reverse(jobList);
    for (UpdateCenterJob job : jobList) {
      if (job instanceof InstallationJob) {
        InstallationJob ij = (InstallationJob)job;
        if (ij.plugin.name.equals(plugin.name) && ij.plugin.sourceId.equals(plugin.sourceId))
          return ij; 
      } 
    } 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Badge getBadge() {
    if (!isSiteDataReady())
      return null; 
    List<UpdateSite.Plugin> plugins = getUpdates();
    int size = plugins.size();
    if (size > 0) {
      StringBuilder tooltip = new StringBuilder();
      Badge.Severity severity = Badge.Severity.WARNING;
      int securityFixSize = (int)plugins.stream().filter(plugin -> plugin.fixesSecurityVulnerabilities()).count();
      int incompatibleSize = (int)plugins.stream().filter(plugin -> !plugin.isCompatibleWithInstalledVersion()).count();
      if (size > 1) {
        tooltip.append(Messages.PluginsLink_updatesAvailable(Integer.valueOf(size)));
      } else {
        tooltip.append(Messages.PluginsLink_updateAvailable());
      } 
      switch (incompatibleSize) {
        case 0:
          break;
        case 1:
          tooltip.append("\n").append(Messages.PluginsLink_incompatibleUpdateAvailable());
          break;
        default:
          tooltip.append("\n").append(Messages.PluginsLink_incompatibleUpdatesAvailable(Integer.valueOf(incompatibleSize)));
          break;
      } 
      switch (securityFixSize) {
        case 0:
          return new Badge(Integer.toString(size), tooltip.toString(), severity);
        case 1:
          tooltip.append("\n").append(Messages.PluginsLink_securityUpdateAvailable());
          severity = Badge.Severity.DANGER;
      } 
      tooltip.append("\n").append(Messages.PluginsLink_securityUpdatesAvailable(Integer.valueOf(securityFixSize)));
      severity = Badge.Severity.DANGER;
    } 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doConnectionStatus(StaplerRequest request) {
    Jenkins.get().checkPermission(Jenkins.SYSTEM_READ);
    try {
      String siteId = request.getParameter("siteId");
      if (siteId == null) {
        siteId = ID_DEFAULT;
      } else if (siteId.equals("default")) {
        siteId = ID_DEFAULT;
      } 
      ConnectionCheckJob checkJob = getConnectionCheckJob(siteId);
      if (checkJob == null) {
        UpdateSite site = getSite(siteId);
        if (site != null)
          checkJob = addConnectionCheckJob(site); 
      } 
      if (checkJob != null) {
        boolean isOffline = false;
        for (ConnectionStatus status : checkJob.connectionStates.values()) {
          if (ConnectionStatus.FAILED.equals(status)) {
            isOffline = true;
            break;
          } 
        } 
        if (isOffline) {
          checkJob.run();
          isOffline = false;
          for (ConnectionStatus status : checkJob.connectionStates.values()) {
            if (ConnectionStatus.FAILED.equals(status)) {
              isOffline = true;
              break;
            } 
          } 
          if (!isOffline)
            updateAllSites(); 
        } 
        return HttpResponses.okJSON(checkJob.connectionStates);
      } 
      return HttpResponses.errorJSON(String.format("Cannot check connection status of the update site with ID='%s'. This update center cannot be resolved", new Object[] { siteId }));
    } catch (Exception e) {
      return HttpResponses.errorJSON(String.format("ERROR: %s", new Object[] { e.getMessage() }));
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doIncompleteInstallStatus() {
    try {
      Map<String, String> jobs = InstallUtil.getPersistedInstallStatus();
      if (jobs == null)
        jobs = Collections.emptyMap(); 
      return HttpResponses.okJSON(jobs);
    } catch (RuntimeException e) {
      return HttpResponses.errorJSON(String.format("ERROR: %s", new Object[] { e.getMessage() }));
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void persistInstallStatus() {
    List<UpdateCenterJob> jobs = getJobs();
    boolean activeInstalls = false;
    for (UpdateCenterJob job : jobs) {
      if (job instanceof InstallationJob) {
        InstallationJob installationJob = (InstallationJob)job;
        if (!installationJob.status.isSuccess())
          activeInstalls = true; 
      } 
    } 
    if (activeInstalls) {
      InstallUtil.persistInstallStatus(jobs);
    } else {
      InstallUtil.clearInstallStatus();
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doInstallStatus(StaplerRequest request) {
    try {
      String correlationId = request.getParameter("correlationId");
      Map<String, Object> response = new HashMap<String, Object>();
      response.put("state", Jenkins.get().getInstallState().name());
      List<Map<String, String>> installStates = new ArrayList<Map<String, String>>();
      response.put("jobs", installStates);
      List<UpdateCenterJob> jobCopy = getJobs();
      for (UpdateCenterJob job : jobCopy) {
        if (job instanceof InstallationJob) {
          UUID jobCorrelationId = job.getCorrelationId();
          if (correlationId == null || (jobCorrelationId != null && correlationId.equals(jobCorrelationId.toString()))) {
            InstallationJob installationJob = (InstallationJob)job;
            Map<String, String> pluginInfo = new LinkedHashMap<String, String>();
            pluginInfo.put("name", installationJob.plugin.name);
            pluginInfo.put("version", installationJob.plugin.version);
            pluginInfo.put("title", installationJob.plugin.title);
            pluginInfo.put("installStatus", installationJob.status.getType());
            pluginInfo.put("requiresRestart", Boolean.toString(installationJob.status.requiresRestart()));
            if (jobCorrelationId != null)
              pluginInfo.put("correlationId", jobCorrelationId.toString()); 
            installStates.add(pluginInfo);
          } 
        } 
      } 
      return HttpResponses.okJSON(JSONObject.fromObject(response));
    } catch (RuntimeException e) {
      return HttpResponses.errorJSON(String.format("ERROR: %s", new Object[] { e.getMessage() }));
    } 
  }
  
  public HudsonUpgradeJob getHudsonJob() {
    List<UpdateCenterJob> jobList = getJobs();
    Collections.reverse(jobList);
    for (UpdateCenterJob job : jobList) {
      if (job instanceof HudsonUpgradeJob)
        return (HudsonUpgradeJob)job; 
    } 
    return null;
  }
  
  @StaplerDispatchable
  public PersistedList<UpdateSite> getSites() { return this.sites; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isSiteDataReady() {
    if (this.sites.stream().anyMatch(UpdateSite::hasUnparsedData)) {
      if (!this.siteDataLoading) {
        this.siteDataLoading = true;
        Timer.get().submit(() -> {
              this.sites.forEach(UpdateSite::getData);
              this.siteDataLoading = false;
            });
      } 
      return false;
    } 
    return true;
  }
  
  @Exported(name = "sites")
  public List<UpdateSite> getSiteList() { return this.sites.toList(); }
  
  @CheckForNull
  public UpdateSite getSite(String id) { return getById(id); }
  
  public String getLastUpdatedString() {
    long newestTs = 0L;
    for (UpdateSite s : this.sites) {
      if (s.getDataTimestamp() > newestTs)
        newestTs = s.getDataTimestamp(); 
    } 
    if (newestTs == 0L)
      return Messages.UpdateCenter_n_a(); 
    return Util.getTimeSpanString(System.currentTimeMillis() - newestTs);
  }
  
  @CheckForNull
  public UpdateSite getById(String id) {
    for (UpdateSite s : this.sites) {
      if (s.getId().equals(id))
        return s; 
    } 
    return null;
  }
  
  @CheckForNull
  public UpdateSite getCoreSource() {
    for (UpdateSite s : this.sites) {
      UpdateSite.Data data = s.getData();
      if (data != null && data.core != null)
        return s; 
    } 
    return null;
  }
  
  @Deprecated
  public String getDefaultBaseUrl() { return this.config.getUpdateCenterUrl(); }
  
  @CheckForNull
  public UpdateSite.Plugin getPlugin(String artifactId) {
    for (UpdateSite s : this.sites) {
      UpdateSite.Plugin p = s.getPlugin(artifactId);
      if (p != null)
        return p; 
    } 
    return null;
  }
  
  @CheckForNull
  public UpdateSite.Plugin getPlugin(String artifactId, @CheckForNull VersionNumber minVersion) {
    if (minVersion == null)
      return getPlugin(artifactId); 
    for (UpdateSite s : this.sites) {
      UpdateSite.Plugin p = s.getPlugin(artifactId);
      if (checkMinVersion(p, minVersion))
        return p; 
    } 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public List<UpdateSite.Plugin> getPluginFromAllSites(String artifactId, @CheckForNull VersionNumber minVersion) {
    ArrayList<UpdateSite.Plugin> result = new ArrayList<UpdateSite.Plugin>();
    for (UpdateSite s : this.sites) {
      UpdateSite.Plugin p = s.getPlugin(artifactId);
      if (checkMinVersion(p, minVersion))
        result.add(p); 
    } 
    return result;
  }
  
  private boolean checkMinVersion(@CheckForNull UpdateSite.Plugin p, @CheckForNull VersionNumber minVersion) {
    return (p != null && (minVersion == null || !minVersion.isNewerThan(new VersionNumber(p.version))));
  }
  
  @RequirePOST
  public void doUpgrade(StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    HudsonUpgradeJob job = new HudsonUpgradeJob(this, getCoreSource(), Jenkins.getAuthentication2());
    if (!Lifecycle.get().canRewriteHudsonWar()) {
      sendError("Jenkins upgrade not supported in this running mode");
      return;
    } 
    LOGGER.info("Scheduling the core upgrade");
    addJob(job);
    rsp.sendRedirect2(".");
  }
  
  @RequirePOST
  public HttpResponse doInvalidateData() {
    for (UpdateSite site : this.sites)
      site.doInvalidateData(); 
    return HttpResponses.ok();
  }
  
  @RequirePOST
  public void doSafeRestart(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    synchronized (this.jobs) {
      if (!isRestartScheduled()) {
        addJob(new RestartJenkinsJob(this, getCoreSource()));
        LOGGER.info("Scheduling Jenkins reboot");
      } 
    } 
    response.sendRedirect2(".");
  }
  
  @RequirePOST
  public void doCancelRestart(StaplerResponse response) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    synchronized (this.jobs) {
      for (UpdateCenterJob job : this.jobs) {
        if (job instanceof RestartJenkinsJob && ((RestartJenkinsJob)job).cancel())
          LOGGER.info("Scheduled Jenkins reboot unscheduled"); 
      } 
    } 
    response.sendRedirect2(".");
  }
  
  @Exported
  public boolean isRestartRequiredForCompletion() { return this.requiresRestart; }
  
  public boolean isRestartScheduled() {
    for (UpdateCenterJob job : getJobs()) {
      if (job instanceof RestartJenkinsJob) {
        RestartJenkinsJob.RestartJenkinsJobStatus status = ((RestartJenkinsJob)job).status;
        if (status instanceof RestartJenkinsJob.Pending || status instanceof RestartJenkinsJob.Running)
          return true; 
      } 
    } 
    return false;
  }
  
  public boolean isDowngradable() { return (new File("" + Lifecycle.get().getHudsonWar() + ".bak")).exists(); }
  
  @RequirePOST
  public void doDowngrade(StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (!isDowngradable()) {
      sendError("Jenkins downgrade is not possible, probably backup does not exist");
      return;
    } 
    HudsonDowngradeJob job = new HudsonDowngradeJob(this, getCoreSource(), Jenkins.getAuthentication2());
    LOGGER.info("Scheduling the core downgrade");
    addJob(job);
    rsp.sendRedirect2(".");
  }
  
  @RequirePOST
  public void doRestart(StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    HudsonDowngradeJob job = new HudsonDowngradeJob(this, getCoreSource(), Jenkins.getAuthentication2());
    LOGGER.info("Scheduling the core downgrade");
    addJob(job);
    rsp.sendRedirect2(".");
  }
  
  public String getBackupVersion() {
    try {
      JarFile backupWar = new JarFile(new File("" + Lifecycle.get().getHudsonWar() + ".bak"));
      try {
        Attributes attrs = backupWar.getManifest().getMainAttributes();
        String v = attrs.getValue("Jenkins-Version");
        if (v == null)
          v = attrs.getValue("Hudson-Version"); 
        String str = v;
        backupWar.close();
        return str;
      } catch (Throwable throwable) {
        try {
          backupWar.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to read backup version ", e);
      return null;
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Future<UpdateCenterJob> addJob(UpdateCenterJob job) {
    if (job.site != null)
      addConnectionCheckJob(job.site); 
    return job.submit();
  }
  
  @NonNull
  private ConnectionCheckJob addConnectionCheckJob(@NonNull UpdateSite site) {
    if (this.sourcesUsed.add(site)) {
      ConnectionCheckJob connectionCheckJob = newConnectionCheckJob(site);
      connectionCheckJob.submit();
      return connectionCheckJob;
    } 
    ConnectionCheckJob connectionCheckJob = getConnectionCheckJob(site);
    if (connectionCheckJob != null)
      return connectionCheckJob; 
    throw new IllegalStateException("Illegal addition of an UpdateCenter job without calling UpdateCenter.addJob. No ConnectionCheckJob found for the site.");
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  ConnectionCheckJob newConnectionCheckJob(UpdateSite site) { return new ConnectionCheckJob(this, site); }
  
  @CheckForNull
  private ConnectionCheckJob getConnectionCheckJob(@NonNull String siteId) {
    UpdateSite site = getSite(siteId);
    if (site == null)
      return null; 
    return getConnectionCheckJob(site);
  }
  
  @CheckForNull
  private ConnectionCheckJob getConnectionCheckJob(@NonNull UpdateSite site) {
    synchronized (this.jobs) {
      for (UpdateCenterJob job : this.jobs) {
        if (job instanceof ConnectionCheckJob && job.site != null && job.site.getId().equals(site.getId()))
          return (ConnectionCheckJob)job; 
      } 
    } 
    return null;
  }
  
  public String getDisplayName() { return Messages.UpdateCenter_DisplayName(); }
  
  public String getSearchUrl() { return "updateCenter"; }
  
  public void save() {
    if (BulkChange.contains(this))
      return; 
    try {
      getConfigFile().write(this.sites);
      SaveableListener.fireOnChange(this, getConfigFile());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to save " + getConfigFile(), e);
    } 
  }
  
  public void load() {
    XmlFile file = getConfigFile();
    if (file.exists()) {
      try {
        this.sites.replaceBy(((PersistedList)file.unmarshal(this.sites)).toList());
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to load " + file, e);
      } 
      boolean defaultSiteExists = false;
      for (UpdateSite site : this.sites) {
        if (site.isLegacyDefault()) {
          this.sites.remove(site);
          continue;
        } 
        if (ID_DEFAULT.equals(site.getId()))
          defaultSiteExists = true; 
      } 
      if (!defaultSiteExists)
        this.sites.add(createDefaultUpdateSite()); 
    } else if (this.sites.isEmpty()) {
      this.sites.add(createDefaultUpdateSite());
    } 
    this.siteDataLoading = false;
  }
  
  protected UpdateSite createDefaultUpdateSite() {
    return new UpdateSite("default", this.config.getUpdateCenterUrl() + "update-center.json");
  }
  
  private XmlFile getConfigFile() {
    return new XmlFile(XSTREAM, new File((Jenkins.get()).root, UpdateCenter.class.getName() + ".xml"));
  }
  
  @Exported
  public List<UpdateSite.Plugin> getAvailables() {
    Map<String, UpdateSite.Plugin> pluginMap = new LinkedHashMap<String, UpdateSite.Plugin>();
    for (UpdateSite site : this.sites) {
      for (UpdateSite.Plugin plugin : site.getAvailables()) {
        UpdateSite.Plugin existing = (UpdateSite.Plugin)pluginMap.get(plugin.name);
        if (existing == null) {
          pluginMap.put(plugin.name, plugin);
          continue;
        } 
        if (!existing.version.equals(plugin.version)) {
          String altKey = plugin.name + ":" + plugin.name;
          if (!pluginMap.containsKey(altKey))
            pluginMap.put(altKey, plugin); 
        } 
      } 
    } 
    return new ArrayList(pluginMap.values());
  }
  
  @Deprecated
  public PluginEntry[] getCategorizedAvailables() {
    TreeSet<PluginEntry> entries = new TreeSet<PluginEntry>();
    for (UpdateSite.Plugin p : getAvailables()) {
      if (p.categories == null || p.categories.length == 0) {
        entries.add(new PluginEntry(p, getCategoryDisplayName(null)));
        continue;
      } 
      for (String c : p.categories)
        entries.add(new PluginEntry(p, getCategoryDisplayName(c))); 
    } 
    return (PluginEntry[])entries.toArray(new PluginEntry[0]);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getCategoryDisplayName(String category) {
    if (category == null)
      return Messages.UpdateCenter_PluginCategory_misc(); 
    try {
      return (String)Messages.class.getMethod("UpdateCenter_PluginCategory_" + category.replace('-', '_'), new Class[0]).invoke(null, new Object[0]);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      return category;
    } 
  }
  
  public List<UpdateSite.Plugin> getUpdates() {
    Map<String, UpdateSite.Plugin> pluginMap = new LinkedHashMap<String, UpdateSite.Plugin>();
    Map<String, Set<UpdateSite.Plugin>> incompatiblePluginMap = new LinkedHashMap<String, Set<UpdateSite.Plugin>>();
    PluginManager.MetadataCache cache = new PluginManager.MetadataCache();
    for (UpdateSite site : this.sites) {
      for (UpdateSite.Plugin plugin : site.getUpdates()) {
        UpdateSite.Plugin existing = (UpdateSite.Plugin)pluginMap.get(plugin.name);
        if (existing == null) {
          pluginMap.put(plugin.name, plugin);
          if (!plugin.isNeededDependenciesCompatibleWithInstalledVersion())
            for (UpdateSite.Plugin incompatiblePlugin : plugin.getDependenciesIncompatibleWithInstalledVersion(cache))
              ((Set)incompatiblePluginMap.computeIfAbsent(incompatiblePlugin.name, _ignored -> new HashSet())).add(plugin);  
          continue;
        } 
        if (!existing.version.equals(plugin.version)) {
          String altKey = plugin.name + ":" + plugin.name;
          if (!pluginMap.containsKey(altKey))
            pluginMap.put(altKey, plugin); 
        } 
      } 
    } 
    incompatiblePluginMap.forEach((key, incompatiblePlugins) -> pluginMap.computeIfPresent(key, ()));
    return new ArrayList(pluginMap.values());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasIncompatibleUpdates(PluginManager.MetadataCache cache) { return getUpdates().stream().anyMatch(plugin -> !plugin.isCompatible(cache)); }
  
  public List<FormValidation> updateAllSites() throws InterruptedException, ExecutionException {
    List<Future<FormValidation>> futures = new ArrayList<Future<FormValidation>>();
    for (UpdateSite site : getSites()) {
      Future<FormValidation> future = site.updateDirectly();
      if (future != null)
        futures.add(future); 
    } 
    List<FormValidation> results = new ArrayList<FormValidation>();
    for (Future<FormValidation> f : futures)
      results.add((FormValidation)f.get()); 
    return results;
  }
  
  private static VerificationResult verifyChecksums(String expectedDigest, String actualDigest, boolean caseSensitive) {
    if (expectedDigest == null)
      return VerificationResult.NOT_PROVIDED; 
    if (actualDigest == null)
      return VerificationResult.NOT_COMPUTED; 
    if (caseSensitive) {
      if (MessageDigest.isEqual(expectedDigest.getBytes(StandardCharsets.US_ASCII), actualDigest.getBytes(StandardCharsets.US_ASCII)))
        return VerificationResult.PASS; 
    } else if (MessageDigest.isEqual(expectedDigest.toLowerCase().getBytes(StandardCharsets.US_ASCII), actualDigest.toLowerCase().getBytes(StandardCharsets.US_ASCII))) {
      return VerificationResult.PASS;
    } 
    return VerificationResult.FAIL;
  }
  
  private static void throwVerificationFailure(String expected, String actual, File file, String algorithm) throws IOException {
    throw new IOException("Downloaded file " + file.getAbsolutePath() + " does not match expected " + algorithm + ", expected '" + expected + "', actual '" + actual + "'");
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  static void verifyChecksums(WithComputedChecksums job, UpdateSite.Entry entry, File file) throws IOException {
    VerificationResult result512 = verifyChecksums(entry.getSha512(), job.getComputedSHA512(), false);
    switch (null.$SwitchMap$hudson$model$UpdateCenter$VerificationResult[result512.ordinal()]) {
      case 1:
        return;
      case 2:
        throwVerificationFailure(entry.getSha512(), job.getComputedSHA512(), file, "SHA-512");
        break;
      case 3:
        LOGGER.log(Level.WARNING, "Attempt to verify a downloaded file (" + file.getName() + ") using SHA-512 failed since it could not be computed. Falling back to weaker algorithms. Update your JRE.");
        break;
      case 4:
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + result512);
    } 
    VerificationResult result256 = verifyChecksums(entry.getSha256(), job.getComputedSHA256(), false);
    switch (null.$SwitchMap$hudson$model$UpdateCenter$VerificationResult[result256.ordinal()]) {
      case 1:
        return;
      case 2:
        throwVerificationFailure(entry.getSha256(), job.getComputedSHA256(), file, "SHA-256");
        break;
      case 3:
      case 4:
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + result256);
    } 
    if (result512 == VerificationResult.NOT_PROVIDED && result256 == VerificationResult.NOT_PROVIDED)
      LOGGER.log(Level.INFO, "Attempt to verify a downloaded file (" + file.getName() + ") using SHA-512 or SHA-256 failed since your configured update site does not provide either of those checksums. Falling back to SHA-1."); 
    VerificationResult result1 = verifyChecksums(entry.getSha1(), job.getComputedSHA1(), true);
    switch (null.$SwitchMap$hudson$model$UpdateCenter$VerificationResult[result1.ordinal()]) {
      case 1:
        return;
      case 2:
        throwVerificationFailure(entry.getSha1(), job.getComputedSHA1(), file, "SHA-1");
        return;
      case 3:
        throw new IOException("Failed to compute SHA-1 of downloaded file, refusing installation");
      case 4:
        throw new IOException("Unable to confirm integrity of downloaded file, refusing installation");
    } 
    throw new AssertionError("Unknown verification result: " + result1);
  }
  
  @Initializer(after = InitMilestone.PLUGINS_STARTED, fatal = false)
  public static void init(Jenkins h) throws IOException { h.getUpdateCenter().load(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void updateAllSitesNow() {
    for (UpdateSite site : Jenkins.get().getUpdateCenter().getSites()) {
      try {
        site.updateDirectlyNow();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, MessageFormat.format("Failed to update the update site ''{0}''. Plugin upgrades may fail.", new Object[] { site.getId() }), e);
      } 
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void updateDefaultSite() {
    site = Jenkins.get().getUpdateCenter().getSite(ID_DEFAULT);
    if (site == null) {
      LOGGER.log(Level.SEVERE, "Upgrading Jenkins. Cannot retrieve the default Update Site ''{0}''. Plugin installation may fail.", ID_DEFAULT);
      return;
    } 
    try {
      site.updateDirectlyNow();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Upgrading Jenkins. Failed to update the default Update Site '" + ID_DEFAULT + "'. Plugin upgrades may fail.", e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.SYSTEM_READ); 
    return this;
  }
  
  private static void moveAtomically(File src, File target) throws IOException {
    try {
      Files.move(Util.fileToPath(src), Util.fileToPath(target), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE });
    } catch (AtomicMoveNotSupportedException e) {
      LOGGER.log(Level.WARNING, "Atomic move not supported. Falling back to non-atomic move.", e);
      try {
        Files.move(Util.fileToPath(src), Util.fileToPath(target), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
      } catch (IOException e2) {
        e2.addSuppressed(e);
        throw e2;
      } 
    } 
  }
}
