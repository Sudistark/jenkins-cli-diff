package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.slaves.RemotingWorkDirSettings;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class JNLPLauncher extends ComputerLauncher {
  @CheckForNull
  public final String tunnel;
  
  @Deprecated
  public final String vmargs;
  
  @NonNull
  private RemotingWorkDirSettings workDirSettings;
  
  private boolean webSocket;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public static final String CUSTOM_INBOUND_URL_PROPERTY = "jenkins.agent.inboundUrl";
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Descriptor<ComputerLauncher> DESCRIPTOR;
  
  @Deprecated
  public JNLPLauncher(@CheckForNull String tunnel, @CheckForNull String vmargs, @CheckForNull RemotingWorkDirSettings workDirSettings) {
    this(tunnel, vmargs);
    if (workDirSettings != null)
      setWorkDirSettings(workDirSettings); 
  }
  
  @DataBoundConstructor
  public JNLPLauncher(@CheckForNull String tunnel) {
    this.vmargs = null;
    this.workDirSettings = RemotingWorkDirSettings.getEnabledDefaults();
    this.tunnel = Util.fixEmptyAndTrim(tunnel);
  }
  
  @Deprecated
  public JNLPLauncher(@CheckForNull String tunnel, @CheckForNull String vmargs) {
    this.vmargs = null;
    this.workDirSettings = RemotingWorkDirSettings.getEnabledDefaults();
    this.tunnel = Util.fixEmptyAndTrim(tunnel);
  }
  
  @Deprecated
  public JNLPLauncher() { this(false); }
  
  public JNLPLauncher(boolean enableWorkDir) {
    this(null, null, enableWorkDir ? 
        RemotingWorkDirSettings.getEnabledDefaults() : 
        RemotingWorkDirSettings.getDisabledDefaults());
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "workDirSettings in readResolve is needed for data migration.")
  protected Object readResolve() {
    if (this.workDirSettings == null)
      this.workDirSettings = RemotingWorkDirSettings.getDisabledDefaults(); 
    return this;
  }
  
  @NonNull
  public RemotingWorkDirSettings getWorkDirSettings() { return this.workDirSettings; }
  
  @DataBoundSetter
  public final void setWorkDirSettings(@NonNull RemotingWorkDirSettings workDirSettings) { this.workDirSettings = workDirSettings; }
  
  public boolean isLaunchSupported() { return false; }
  
  public boolean isWebSocket() { return this.webSocket; }
  
  @DataBoundSetter
  public void setWebSocket(boolean webSocket) { this.webSocket = webSocket; }
  
  public void launch(SlaveComputer computer, TaskListener listener) {}
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public String getWorkDirOptions(@NonNull Computer computer) {
    if (!(computer instanceof SlaveComputer))
      return ""; 
    return this.workDirSettings.toCommandLineString((SlaveComputer)computer);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getInboundAgentUrl() {
    url = SystemProperties.getString("jenkins.agent.inboundUrl");
    if (url == null || url.isEmpty())
      return Jenkins.get().getRootUrl(); 
    return url;
  }
}
