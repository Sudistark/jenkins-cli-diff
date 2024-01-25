package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.slaves.SlaveComputer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

public class RemotingWorkDirSettings extends Object implements Describable<RemotingWorkDirSettings> {
  private static final String DEFAULT_INTERNAL_DIR = "remoting";
  
  private static final RemotingWorkDirSettings LEGACY_DEFAULT = new RemotingWorkDirSettings(true, null, "remoting", false);
  
  private static final RemotingWorkDirSettings ENABLED_DEFAULT = new RemotingWorkDirSettings(false, null, "remoting", false);
  
  private final boolean disabled;
  
  @CheckForNull
  private final String workDirPath;
  
  @NonNull
  private final String internalDir;
  
  private final boolean failIfWorkDirIsMissing;
  
  @DataBoundConstructor
  public RemotingWorkDirSettings(boolean disabled, @CheckForNull String workDirPath, @CheckForNull String internalDir, boolean failIfWorkDirIsMissing) {
    this.disabled = disabled;
    this.workDirPath = Util.fixEmptyAndTrim(workDirPath);
    this.failIfWorkDirIsMissing = failIfWorkDirIsMissing;
    String internalDirName = Util.fixEmptyAndTrim(internalDir);
    this.internalDir = (internalDirName != null) ? internalDirName : "remoting";
  }
  
  public RemotingWorkDirSettings() { this(false, null, "remoting", false); }
  
  public boolean isDisabled() { return this.disabled; }
  
  public boolean isUseAgentRootDir() { return (this.workDirPath == null); }
  
  public boolean isFailIfWorkDirIsMissing() { return this.failIfWorkDirIsMissing; }
  
  @CheckForNull
  public String getWorkDirPath() { return this.workDirPath; }
  
  @NonNull
  public String getInternalDir() { return this.internalDir; }
  
  public Descriptor<RemotingWorkDirSettings> getDescriptor() { return Jenkins.get().getDescriptor(RemotingWorkDirSettings.class); }
  
  public List<String> toCommandLineArgs(@NonNull SlaveComputer computer) {
    if (this.disabled)
      return Collections.emptyList(); 
    ArrayList<String> args = new ArrayList<String>();
    args.add("-workDir");
    if (this.workDirPath == null) {
      Slave node = computer.getNode();
      if (node == null)
        return Collections.emptyList(); 
      args.add(node.getRemoteFS());
    } else {
      args.add(this.workDirPath);
    } 
    if (!"remoting".equals(this.internalDir)) {
      args.add("-internalDir");
      args.add(this.internalDir);
    } 
    if (this.failIfWorkDirIsMissing)
      args.add(" -failIfWorkDirIsMissing"); 
    return Collections.unmodifiableList(args);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public String toCommandLineString(@NonNull SlaveComputer computer) {
    if (this.disabled)
      return ""; 
    StringBuilder bldr = new StringBuilder();
    bldr.append("-workDir \"");
    if (this.workDirPath == null) {
      Slave node = computer.getNode();
      if (node == null)
        return ""; 
      bldr.append(node.getRemoteFS());
    } else {
      bldr.append(this.workDirPath);
    } 
    bldr.append("\"");
    if (!"remoting".equals(this.internalDir)) {
      bldr.append(" -internalDir \"");
      bldr.append(this.internalDir);
      bldr.append("\"");
    } 
    if (this.failIfWorkDirIsMissing)
      bldr.append(" -failIfWorkDirIsMissing"); 
    return bldr.toString();
  }
  
  @NonNull
  public static RemotingWorkDirSettings getDisabledDefaults() { return LEGACY_DEFAULT; }
  
  @NonNull
  public static RemotingWorkDirSettings getEnabledDefaults() { return ENABLED_DEFAULT; }
}
