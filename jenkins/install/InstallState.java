package jenkins.install;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.UpdateCenter;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.security.stapler.StaplerAccessibleType;
import jenkins.util.Timer;
import org.apache.commons.lang.StringUtils;

@StaplerAccessibleType
public class InstallState implements ExtensionPoint {
  @Deprecated
  private static final InstallState[] UNUSED_INNER_CLASSES = { new Object("UNKNOWN", false), new Object("INITIAL_SETUP_COMPLETED", false), new Object("CREATE_ADMIN_USER", false), new Object("INITIAL_SECURITY_SETUP", false), new Object("RESTART", false), new Object("DOWNGRADE", false) };
  
  @Extension
  public static final InstallState UNKNOWN = new Unknown();
  
  @Extension
  public static final InstallState RUNNING = new InstallState("RUNNING", true);
  
  @Extension
  public static final InstallState INITIAL_SETUP_COMPLETED = new InitialSetupCompleted();
  
  @Extension
  public static final InstallState CREATE_ADMIN_USER = new CreateAdminUser();
  
  @Extension
  public static final InstallState CONFIGURE_INSTANCE = new ConfigureInstance();
  
  @Extension
  public static final InstallState INITIAL_PLUGINS_INSTALLING = new InstallState("INITIAL_PLUGINS_INSTALLING", false);
  
  @Extension
  public static final InstallState INITIAL_SECURITY_SETUP = new InitialSecuritySetup();
  
  @Extension
  public static final InstallState NEW = new InstallState("NEW", false);
  
  @Extension
  public static final InstallState RESTART = new Restart();
  
  @Extension
  public static final InstallState UPGRADE = new Upgrade();
  
  private static void reloadUpdateSiteData() { Timer.get().submit(UpdateCenter::updateAllSitesNow); }
  
  @Extension
  public static final InstallState DOWNGRADE = new Downgrade();
  
  private static final Logger LOGGER = Logger.getLogger(InstallState.class.getName());
  
  public static final InstallState TEST = new InstallState("TEST", true);
  
  public static final InstallState DEVELOPMENT = new InstallState("DEVELOPMENT", true);
  
  private final boolean isSetupComplete;
  
  private final String name;
  
  public InstallState(@NonNull String name, boolean isSetupComplete) {
    this.name = name;
    this.isSetupComplete = isSetupComplete;
  }
  
  public void initializeState() {}
  
  @Deprecated
  protected Object readResolve() {
    if (StringUtils.isBlank(this.name)) {
      LOGGER.log(Level.WARNING, "Read install state with blank name: ''{0}''. It will be ignored", this.name);
      return UNKNOWN;
    } 
    InstallState state = valueOf(this.name);
    if (state == null) {
      LOGGER.log(Level.WARNING, "Cannot locate an extension point for the state ''{0}''. It will be ignored", this.name);
      return UNKNOWN;
    } 
    return state;
  }
  
  public boolean isSetupComplete() { return this.isSetupComplete; }
  
  public String name() { return this.name; }
  
  public int hashCode() { return this.name.hashCode(); }
  
  public boolean equals(Object obj) {
    if (obj instanceof InstallState)
      return this.name.equals(((InstallState)obj).name()); 
    return false;
  }
  
  public String toString() { return "InstallState (" + this.name + ")"; }
  
  @CheckForNull
  public static InstallState valueOf(@NonNull String name) {
    for (InstallState state : all()) {
      if (name.equals(state.name))
        return state; 
    } 
    return null;
  }
  
  static ExtensionList<InstallState> all() { return ExtensionList.lookup(InstallState.class); }
}
