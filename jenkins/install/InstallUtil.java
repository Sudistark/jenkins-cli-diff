package jenkins.install;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;
import hudson.Util;
import hudson.model.UpdateCenter;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import jakarta.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class InstallUtil {
  private static final Logger LOGGER = Logger.getLogger(InstallUtil.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final VersionNumber NEW_INSTALL_VERSION = new VersionNumber("1.0");
  
  private static final VersionNumber FORCE_NEW_INSTALL_VERSION = new VersionNumber("0.0");
  
  public static void proceedToNextStateFrom(InstallState prior) {
    InstallState next = getNextInstallState(prior);
    if (next != null)
      Jenkins.get().setInstallState(next); 
  }
  
  static InstallState getNextInstallState(InstallState current) {
    List<Function<Provider<InstallState>, InstallState>> installStateFilterChain = new ArrayList<Function<Provider<InstallState>, InstallState>>();
    for (Iterator iterator = InstallStateFilter.all().iterator(); iterator.hasNext(); ) {
      InstallStateFilter setupExtension = (InstallStateFilter)iterator.next();
      installStateFilterChain.add(next -> setupExtension.getNextInstallState(current, next));
    } 
    installStateFilterChain.add(input -> {
          if (current == null || InstallState.UNKNOWN.equals(current))
            return getDefaultInstallState(); 
          Map<InstallState, InstallState> states = new HashMap<InstallState, InstallState>();
          states.put(InstallState.CONFIGURE_INSTANCE, InstallState.INITIAL_SETUP_COMPLETED);
          states.put(InstallState.CREATE_ADMIN_USER, InstallState.CONFIGURE_INSTANCE);
          states.put(InstallState.INITIAL_PLUGINS_INSTALLING, InstallState.CREATE_ADMIN_USER);
          states.put(InstallState.INITIAL_SECURITY_SETUP, InstallState.NEW);
          states.put(InstallState.RESTART, InstallState.RUNNING);
          states.put(InstallState.UPGRADE, InstallState.INITIAL_SETUP_COMPLETED);
          states.put(InstallState.DOWNGRADE, InstallState.INITIAL_SETUP_COMPLETED);
          states.put(InstallState.INITIAL_SETUP_COMPLETED, InstallState.RUNNING);
          return (InstallState)states.get(current);
        });
    ProviderChain<InstallState> chain = new ProviderChain<InstallState>(installStateFilterChain.iterator());
    return (InstallState)chain.get();
  }
  
  private static InstallState getDefaultInstallState() {
    stateOverride = System.getProperty("jenkins.install.state", System.getenv("jenkins.install.state"));
    if (stateOverride != null)
      try {
        return InstallState.valueOf(stateOverride.toUpperCase());
      } catch (RuntimeException e) {
        throw new IllegalStateException("Unknown install state override specified on the commandline: '" + stateOverride + "'.", e);
      }  
    String shouldRunFlag = SystemProperties.getString("jenkins.install.runSetupWizard");
    boolean shouldRun = "true".equalsIgnoreCase(shouldRunFlag);
    boolean shouldNotRun = "false".equalsIgnoreCase(shouldRunFlag);
    if (!shouldRun) {
      if (Functions.getIsUnitTest())
        return InstallState.TEST; 
      if (SystemProperties.getBoolean("hudson.Main.development"))
        return InstallState.DEVELOPMENT; 
    } 
    VersionNumber lastRunVersion = new VersionNumber(getLastExecVersion());
    if (!SetupWizard.getUpdateStateFile().exists()) {
      Jenkins j = Jenkins.get();
      if (shouldNotRun) {
        InstallState.INITIAL_SETUP_COMPLETED.initializeState();
        return j.getInstallState();
      } 
      return InstallState.INITIAL_SECURITY_SETUP;
    } 
    VersionNumber currentRunVersion = new VersionNumber(getCurrentExecVersion());
    if (lastRunVersion.isOlderThan(currentRunVersion))
      return InstallState.UPGRADE; 
    if (lastRunVersion.isNewerThan(currentRunVersion))
      return InstallState.DOWNGRADE; 
    return InstallState.RESTART;
  }
  
  public static void saveLastExecVersion() {
    if (Jenkins.VERSION.equals("?"))
      throw new IllegalStateException("Unexpected call to InstallUtil.saveLastExecVersion(). Jenkins.VERSION has not been initialized. Call computeVersion() first."); 
    saveLastExecVersion(Jenkins.VERSION);
  }
  
  @NonNull
  public static String getLastExecVersion() {
    lastExecVersionFile = getLastExecVersionFile();
    if (lastExecVersionFile.exists())
      try {
        String version = Files.readString(Util.fileToPath(lastExecVersionFile), Charset.defaultCharset());
        if (StringUtils.isBlank(version))
          return FORCE_NEW_INSTALL_VERSION.toString(); 
        return version;
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Unexpected Error. Unable to read " + lastExecVersionFile.getAbsolutePath(), e);
        LOGGER.log(Level.WARNING, "Unable to determine the last running version (see error above). Treating this as a restart. No plugins will be updated.");
        return getCurrentExecVersion();
      }  
    File configFile = getConfigFile();
    if (configFile.exists())
      try {
        String lastVersion = XMLUtils.getValue("/hudson/version", configFile);
        if (lastVersion.length() > 0) {
          LOGGER.log(Level.FINE, "discovered serialized lastVersion {0}", lastVersion);
          return lastVersion;
        } 
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Unexpected error reading global config.xml", e);
      }  
    return NEW_INSTALL_VERSION.toString();
  }
  
  static void saveLastExecVersion(@NonNull String version) {
    File lastExecVersionFile = getLastExecVersionFile();
    try {
      Files.writeString(Util.fileToPath(lastExecVersionFile), version, Charset.defaultCharset(), new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to save " + lastExecVersionFile.getAbsolutePath(), e);
    } 
  }
  
  static File getConfigFile() { return new File(Jenkins.get().getRootDir(), "config.xml"); }
  
  static File getLastExecVersionFile() { return new File(Jenkins.get().getRootDir(), "jenkins.install.InstallUtil.lastExecVersion"); }
  
  static File getInstallingPluginsFile() { return new File(Jenkins.get().getRootDir(), "jenkins.install.InstallUtil.installingPlugins"); }
  
  private static String getCurrentExecVersion() {
    if (Jenkins.VERSION.equals("?"))
      throw new IllegalStateException("Unexpected call to InstallUtil.getCurrentExecVersion(). Jenkins.VERSION has not been initialized. Call computeVersion() first."); 
    return Jenkins.VERSION;
  }
  
  @CheckForNull
  public static Map<String, String> getPersistedInstallStatus() {
    installingPluginsFile = getInstallingPluginsFile();
    if (installingPluginsFile == null || !installingPluginsFile.exists())
      return null; 
    return (Map)(new XStream2()).fromXML(installingPluginsFile);
  }
  
  public static void persistInstallStatus(List<UpdateCenter.UpdateCenterJob> installingPlugins) {
    File installingPluginsFile = getInstallingPluginsFile();
    if (installingPlugins == null || installingPlugins.isEmpty()) {
      try {
        Files.deleteIfExists(installingPluginsFile.toPath());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } 
      return;
    } 
    LOGGER.fine("Writing install state to: " + installingPluginsFile.getAbsolutePath());
    Map<String, String> statuses = new HashMap<String, String>();
    for (UpdateCenter.UpdateCenterJob j : installingPlugins) {
      if (j instanceof UpdateCenter.InstallationJob && j.getCorrelationId() != null) {
        UpdateCenter.InstallationJob ij = (UpdateCenter.InstallationJob)j;
        UpdateCenter.DownloadJob.InstallationStatus status = ij.status;
        String statusText = status.getType();
        if (status instanceof UpdateCenter.DownloadJob.Installing)
          statusText = "Pending"; 
        statuses.put(ij.plugin.name, statusText);
      } 
    } 
    try {
      String installingPluginXml = (new XStream2()).toXML(statuses);
      Files.writeString(Util.fileToPath(installingPluginsFile), installingPluginXml, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to save " + installingPluginsFile.getAbsolutePath(), e);
    } 
  }
  
  public static void clearInstallStatus() { persistInstallStatus(null); }
}
