package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Launcher;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.model.labels.LabelAtom;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.RetentionStrategy;
import hudson.slaves.SlaveComputer;
import hudson.util.ClockDifference;
import hudson.util.DescribableList;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.slaves.WorkspaceLocator;
import jenkins.util.SystemProperties;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class Slave extends Node implements Serializable {
  private static final Logger LOGGER = Logger.getLogger(Slave.class.getName());
  
  protected String name;
  
  private String description;
  
  protected final String remoteFS;
  
  private int numExecutors;
  
  private Node.Mode mode;
  
  private RetentionStrategy retentionStrategy;
  
  private ComputerLauncher launcher;
  
  private String label;
  
  private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties;
  
  @Deprecated
  private String userId;
  
  @CheckForNull
  private Set<LabelAtom> labelAtomSet;
  
  @Deprecated
  private String agentCommand;
  
  @Deprecated
  protected Slave(String name, String nodeDescription, String remoteFS, String numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException { this(name, nodeDescription, remoteFS, Util.tryParseNumber(numExecutors, Integer.valueOf(1)).intValue(), mode, labelString, launcher, retentionStrategy, nodeProperties); }
  
  @Deprecated
  protected Slave(String name, String nodeDescription, String remoteFS, int numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy) throws Descriptor.FormException, IOException { this(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, new ArrayList()); }
  
  protected Slave(@NonNull String name, String remoteFS, ComputerLauncher launcher) throws Descriptor.FormException, IOException {
    this.numExecutors = 1;
    this.mode = Node.Mode.NORMAL;
    this.label = "";
    this.nodeProperties = new DescribableList(this);
    this.name = name;
    this.remoteFS = remoteFS;
    this.launcher = launcher;
    this.labelAtomSet = Collections.unmodifiableSet(Label.parse(this.label));
  }
  
  @Deprecated
  protected Slave(@NonNull String name, String nodeDescription, String remoteFS, int numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException {
    this.numExecutors = 1;
    this.mode = Node.Mode.NORMAL;
    this.label = "";
    this.nodeProperties = new DescribableList(this);
    this.name = name;
    this.description = nodeDescription;
    this.numExecutors = numExecutors;
    this.mode = mode;
    this.remoteFS = Util.fixNull(remoteFS).trim();
    _setLabelString(labelString);
    this.launcher = launcher;
    this.retentionStrategy = retentionStrategy;
    getAssignedLabels();
    this.nodeProperties.replaceBy(nodeProperties);
    if (name.isEmpty())
      throw new Descriptor.FormException(Messages.Slave_InvalidConfig_NoName(), null); 
    if (this.numExecutors <= 0)
      throw new Descriptor.FormException(Messages.Slave_InvalidConfig_Executors(name), null); 
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.220")
  public String getUserId() { return this.userId; }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.220")
  public void setUserId(String userId) {}
  
  public ComputerLauncher getLauncher() {
    if (this.launcher == null && this.agentCommand != null && !this.agentCommand.isEmpty())
      try {
        this.launcher = (ComputerLauncher)(Jenkins.get().getPluginManager()).uberClassLoader.loadClass("hudson.slaves.CommandLauncher").getConstructor(new Class[] { String.class, hudson.EnvVars.class }).newInstance(new Object[] { this.agentCommand, null });
        this.agentCommand = null;
        save();
      } catch (Exception x) {
        LOGGER.log(Level.WARNING, "could not update historical agentCommand setting to CommandLauncher", x);
      }  
    return (this.launcher == null) ? new JNLPLauncher(false) : this.launcher;
  }
  
  public void setLauncher(ComputerLauncher launcher) { this.launcher = launcher; }
  
  public String getRemoteFS() { return this.remoteFS; }
  
  @NonNull
  public String getNodeName() { return this.name; }
  
  public String toString() {
    return getClass().getName() + "[" + getClass().getName() + "]";
  }
  
  public void setNodeName(String name) { this.name = name; }
  
  @DataBoundSetter
  public void setNodeDescription(String value) { this.description = value; }
  
  public String getNodeDescription() { return this.description; }
  
  public int getNumExecutors() { return this.numExecutors; }
  
  @DataBoundSetter
  public void setNumExecutors(int n) { this.numExecutors = n; }
  
  public Node.Mode getMode() { return this.mode; }
  
  @DataBoundSetter
  public void setMode(Node.Mode mode) { this.mode = mode; }
  
  @NonNull
  public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
    assert this.nodeProperties != null;
    return this.nodeProperties;
  }
  
  @DataBoundSetter
  public void setNodeProperties(List<? extends NodeProperty<?>> properties) throws IOException {
    if (this.nodeProperties == null) {
      warnPlugin();
      this.nodeProperties = new DescribableList(this);
    } 
    this.nodeProperties.replaceBy(properties);
  }
  
  public RetentionStrategy getRetentionStrategy() { return (this.retentionStrategy == null) ? RetentionStrategy.Always.INSTANCE : this.retentionStrategy; }
  
  @DataBoundSetter
  public void setRetentionStrategy(RetentionStrategy availabilityStrategy) { this.retentionStrategy = availabilityStrategy; }
  
  public String getLabelString() { return Util.fixNull(this.label).trim(); }
  
  @DataBoundSetter
  public void setLabelString(String labelString) {
    _setLabelString(labelString);
    getAssignedLabels();
  }
  
  private void _setLabelString(String labelString) {
    this.label = Util.fixNull(labelString).trim();
    this.labelAtomSet = Collections.unmodifiableSet(Label.parse(this.label));
  }
  
  protected Set<LabelAtom> getLabelAtomSet() {
    if (this.labelAtomSet == null) {
      warnPlugin();
      this.labelAtomSet = Collections.unmodifiableSet(Label.parse(this.label));
    } 
    return this.labelAtomSet;
  }
  
  private void warnPlugin() { LOGGER.log(Level.WARNING, () -> getClass().getName() + " or one of its superclass overrides readResolve() without calling super implementation.Please file an issue against the plugin implementing it: " + getClass().getName()); }
  
  public Callable<ClockDifference, IOException> getClockDifferenceCallable() { return new GetClockDifference1(); }
  
  public Computer createComputer() { return new SlaveComputer(this); }
  
  public FilePath getWorkspaceFor(TopLevelItem item) {
    for (WorkspaceLocator l : WorkspaceLocator.all()) {
      FilePath workspace = l.locate(item, this);
      if (workspace != null)
        return workspace; 
    } 
    FilePath r = getWorkspaceRoot();
    if (r == null)
      return null; 
    return r.child(item.getFullName());
  }
  
  @CheckForNull
  public FilePath getRootPath() {
    SlaveComputer computer = getComputer();
    if (computer == null)
      return null; 
    return createPath(StringUtils.defaultString(computer.getAbsoluteRemoteFs(), this.remoteFS));
  }
  
  @CheckForNull
  public FilePath getWorkspaceRoot() {
    FilePath r = getRootPath();
    if (r == null)
      return null; 
    return r.child(WORKSPACE_ROOT);
  }
  
  @NonNull
  public Launcher createLauncher(TaskListener listener) {
    SlaveComputer c = getComputer();
    if (c == null) {
      listener.error("Issue with creating launcher for agent " + this.name + ". Computer has been disconnected");
      return new Launcher.DummyLauncher(listener);
    } 
    Slave node = c.getNode();
    if (node != this) {
      String message = "Issue with creating launcher for agent " + this.name + ". Computer has been reconnected";
      if (LOGGER.isLoggable(Level.WARNING))
        LOGGER.log(Level.WARNING, message, new IllegalStateException("Computer has been reconnected, this Node instance cannot be used anymore")); 
      return new Launcher.DummyLauncher(listener);
    } 
    Channel channel = c.getChannel();
    if (channel == null) {
      reportLauncherCreateError("The agent has not been fully initialized yet", "No remoting channel to the agent OR it has not been fully initialized yet", listener);
      return new Launcher.DummyLauncher(listener);
    } 
    if (channel.isClosingOrClosed()) {
      reportLauncherCreateError("The agent is being disconnected", "Remoting channel is either in the process of closing down or has closed down", listener);
      return new Launcher.DummyLauncher(listener);
    } 
    Boolean isUnix = c.isUnix();
    if (isUnix == null) {
      reportLauncherCreateError("The agent has not been fully initialized yet", "Cannot determine if the agent is a Unix one, the System status request has not completed yet. It is an invalid channel state, please report a bug to Jenkins if you see it.", listener);
      return new Launcher.DummyLauncher(listener);
    } 
    return (new Launcher.RemoteLauncher(listener, channel, isUnix.booleanValue())).decorateFor(this);
  }
  
  private void reportLauncherCreateError(@NonNull String humanReadableMsg, @CheckForNull String exceptionDetails, @NonNull TaskListener listener) {
    String message = "Issue with creating launcher for agent " + this.name + ". " + humanReadableMsg;
    listener.error(message);
    if (LOGGER.isLoggable(Level.WARNING))
      LOGGER.log(Level.WARNING, message + "Probably there is a race condition with Agent reconnection or disconnection, check other log entries", new IllegalStateException(
            
            (exceptionDetails != null) ? exceptionDetails : humanReadableMsg)); 
  }
  
  @CheckForNull
  public SlaveComputer getComputer() { return (SlaveComputer)toComputer(); }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    Slave that = (Slave)o;
    return this.name.equals(that.name);
  }
  
  public int hashCode() { return this.name.hashCode(); }
  
  protected Object readResolve() {
    if (this.nodeProperties == null)
      this.nodeProperties = new DescribableList(this); 
    _setLabelString(this.label);
    return this;
  }
  
  public SlaveDescriptor getDescriptor() {
    Descriptor d = Jenkins.get().getDescriptorOrDie(getClass());
    if (d instanceof SlaveDescriptor)
      return (SlaveDescriptor)d; 
    throw new IllegalStateException("" + d.getClass() + " needs to extend from SlaveDescriptor");
  }
  
  private static final String WORKSPACE_ROOT = SystemProperties.getString(Slave.class.getName() + ".workspaceRoot", "workspace");
  
  private static final Set<String> ALLOWED_JNLPJARS_FILES = Set.of("agent.jar", "slave.jar", "remoting.jar", "jenkins-cli.jar", "hudson-cli.jar");
}
