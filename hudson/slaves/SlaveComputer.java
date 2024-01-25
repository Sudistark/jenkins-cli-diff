package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Functions;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.console.ConsoleLogFilter;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.Messages;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.remoting.Channel;
import hudson.remoting.ChannelBuilder;
import hudson.remoting.CommandTransport;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.Futures;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;
import hudson.util.io.RewindableFileOutputStream;
import hudson.util.io.RewindableRotatingFileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jenkins.agents.AgentComputerUtil;
import jenkins.model.Jenkins;
import jenkins.security.ChannelConfigurator;
import jenkins.slaves.EncryptedSlaveAgentJnlpFile;
import jenkins.slaves.JnlpAgentReceiver;
import jenkins.slaves.RemotingVersionInfo;
import jenkins.slaves.systemInfo.SlaveSystemInfo;
import jenkins.util.Listeners;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class SlaveComputer extends Computer {
  private Charset defaultCharset;
  
  private Boolean isUnix;
  
  private ComputerLauncher launcher;
  
  private final RewindableFileOutputStream log;
  
  private final TaskListener taskListener;
  
  private int numRetryAttempt;
  
  private Object constructed = new Object();
  
  private final Object channelLock;
  
  public SlaveComputer(Slave slave) {
    super(slave);
    this.channelLock = new Object();
    this.log = new RewindableRotatingFileOutputStream(getLogFile(), 10);
    this.taskListener = new StreamTaskListener(decorate(this.log));
    assert slave.getNumExecutors() != 0 : "Computer created with 0 executors";
  }
  
  private OutputStream decorate(OutputStream os) {
    for (ConsoleLogFilter f : ConsoleLogFilter.all()) {
      try {
        os = f.decorateLogger(this, os);
      } catch (IOException|InterruptedException e) {
        LOGGER.log(Level.WARNING, "Failed to filter log with " + f, e);
      } 
    } 
    return os;
  }
  
  @OverrideMustInvoke
  public boolean isAcceptingTasks() { return (this.acceptingTasks && super.isAcceptingTasks()); }
  
  public String getJnlpMac() { return JnlpAgentReceiver.SLAVE_SECRET.mac(getName()); }
  
  public void setAcceptingTasks(boolean acceptingTasks) { this.acceptingTasks = acceptingTasks; }
  
  public Boolean isUnix() { return this.isUnix; }
  
  @CheckForNull
  public Slave getNode() {
    Node node = super.getNode();
    if (node == null || node instanceof Slave)
      return (Slave)node; 
    logger.log(Level.WARNING, "found an unexpected kind of node {0} from {1} with nodeName={2}", new Object[] { node, this, this.nodeName });
    return null;
  }
  
  @NonNull
  public TaskListener getListener() { return this.taskListener; }
  
  public String getIconClassName() {
    Future<?> l = this.lastConnectActivity;
    if (l != null && !l.isDone())
      return "symbol-computer"; 
    return super.getIconClassName();
  }
  
  @Deprecated
  public boolean isJnlpAgent() { return this.launcher instanceof JNLPLauncher; }
  
  public boolean isLaunchSupported() { return this.launcher.isLaunchSupported(); }
  
  public ComputerLauncher getLauncher() { return this.launcher; }
  
  public ComputerLauncher getDelegatedLauncher() {
    ComputerLauncher l = this.launcher;
    while (true) {
      while (l instanceof DelegatingComputerLauncher)
        l = ((DelegatingComputerLauncher)l).getLauncher(); 
      if (l instanceof ComputerLauncherFilter) {
        l = ((ComputerLauncherFilter)l).getCore();
        continue;
      } 
      break;
    } 
    return l;
  }
  
  protected Future<?> _connect(boolean forceReconnect) {
    if (this.channel != null)
      return Futures.precomputed(null); 
    if (!forceReconnect && isConnecting())
      return this.lastConnectActivity; 
    if (forceReconnect && isConnecting())
      logger.fine("Forcing a reconnect on " + getName()); 
    closeChannel();
    Throwable threadInfo = new Throwable("launched here");
    return this.lastConnectActivity = Computer.threadPoolForRemoting.submit(() -> {
          try {
            ACLContext ctx = ACL.as2(ACL.SYSTEM2);
            try {
              this.log.rewind();
              try {
                for (ComputerListener cl : ComputerListener.all())
                  cl.preLaunch(this, this.taskListener); 
                this.offlineCause = null;
                this.launcher.launch(this, this.taskListener);
              } catch (AbortException e) {
                e.addSuppressed(threadInfo);
                this.taskListener.error(e.getMessage());
                throw e;
              } catch (IOException e) {
                e.addSuppressed(threadInfo);
                Util.displayIOException(e, this.taskListener);
                Functions.printStackTrace(e, this.taskListener.error(Messages.ComputerLauncher_unexpectedError()));
                throw e;
              } catch (InterruptedException e) {
                e.addSuppressed(threadInfo);
                Functions.printStackTrace(e, this.taskListener.error(Messages.ComputerLauncher_abortedLaunch()));
                throw e;
              } catch (RuntimeException e) {
                e.addSuppressed(threadInfo);
                Functions.printStackTrace(e, this.taskListener.error(Messages.ComputerLauncher_unexpectedError()));
                throw e;
              } 
              if (ctx != null)
                ctx.close(); 
            } catch (Throwable throwable) {
              if (ctx != null)
                try {
                  ctx.close();
                } catch (Throwable throwable1) {
                  throwable.addSuppressed(throwable1);
                }  
              throw throwable;
            } 
          } finally {
            if (this.channel == null && this.offlineCause == null) {
              this.offlineCause = new OfflineCause.LaunchFailed();
              for (ComputerListener cl : ComputerListener.all())
                cl.onLaunchFailure(this, this.taskListener); 
            } 
          } 
          if (this.channel == null)
            throw new IOException("Agent failed to connect, even though the launcher didn't report it. See the log output for details."); 
          return null;
        });
  }
  
  public void taskAccepted(Executor executor, Queue.Task task) {
    LOGGER.log(Level.FINER, "Accepted {0} on {1}", new Object[] { task.toString(), executor.getOwner().getDisplayName() });
    if (this.launcher instanceof ExecutorListener)
      ((ExecutorListener)this.launcher).taskAccepted(executor, task); 
    Slave node = getNode();
    if (node != null && node.getRetentionStrategy() instanceof ExecutorListener)
      ((ExecutorListener)node.getRetentionStrategy()).taskAccepted(executor, task); 
  }
  
  public void taskStarted(Executor executor, Queue.Task task) {
    LOGGER.log(Level.FINER, "Started {0} on {1}", new Object[] { task.toString(), executor.getOwner().getDisplayName() });
    if (this.launcher instanceof ExecutorListener)
      ((ExecutorListener)this.launcher).taskStarted(executor, task); 
    RetentionStrategy r = getRetentionStrategy();
    if (r instanceof ExecutorListener)
      ((ExecutorListener)r).taskStarted(executor, task); 
  }
  
  public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {
    LOGGER.log(Level.FINE, "Completed {0} on {1}", new Object[] { task.toString(), executor.getOwner().getDisplayName() });
    if (this.launcher instanceof ExecutorListener)
      ((ExecutorListener)this.launcher).taskCompleted(executor, task, durationMS); 
    RetentionStrategy r = getRetentionStrategy();
    if (r instanceof ExecutorListener)
      ((ExecutorListener)r).taskCompleted(executor, task, durationMS); 
  }
  
  public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {
    LOGGER.log(Level.FINE, "Completed with problems {0} on {1}", new Object[] { task.toString(), executor.getOwner().getDisplayName() });
    if (this.launcher instanceof ExecutorListener)
      ((ExecutorListener)this.launcher).taskCompletedWithProblems(executor, task, durationMS, problems); 
    RetentionStrategy r = getRetentionStrategy();
    if (r instanceof ExecutorListener)
      ((ExecutorListener)r).taskCompletedWithProblems(executor, task, durationMS, problems); 
  }
  
  public boolean isConnecting() {
    Future<?> l = this.lastConnectActivity;
    return (isOffline() && l != null && !l.isDone());
  }
  
  public OutputStream openLogFile() {
    try {
      this.log.rewind();
      return this.log;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to create log file " + getLogFile(), e);
      return OutputStream.nullOutputStream();
    } 
  }
  
  public void setChannel(@NonNull InputStream in, @NonNull OutputStream out, @NonNull TaskListener taskListener, @CheckForNull Channel.Listener listener) throws IOException, InterruptedException { setChannel(in, out, taskListener.getLogger(), listener); }
  
  public void setChannel(@NonNull InputStream in, @NonNull OutputStream out, @CheckForNull OutputStream launchLog, @CheckForNull Channel.Listener listener) throws IOException, InterruptedException {
    ChannelBuilder cb = (new ChannelBuilder(this.nodeName, threadPoolForRemoting)).withMode(Channel.Mode.NEGOTIATE).withHeaderStream(launchLog);
    for (ChannelConfigurator cc : ChannelConfigurator.all())
      cc.onChannelBuilding(cb, this); 
    Channel channel = cb.build(in, out);
    setChannel(channel, launchLog, listener);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void setChannel(@NonNull ChannelBuilder cb, @NonNull CommandTransport commandTransport, @CheckForNull Channel.Listener listener) throws IOException, InterruptedException {
    for (ChannelConfigurator cc : ChannelConfigurator.all())
      cc.onChannelBuilding(cb, this); 
    OutputStream headerStream = cb.getHeaderStream();
    if (headerStream == null)
      LOGGER.log(Level.WARNING, "No header stream defined when setting channel for computer {0}. Launch log won't be printed", this); 
    Channel channel = cb.build(commandTransport);
    setChannel(channel, headerStream, listener);
  }
  
  @CheckReturnValue
  public int getClassLoadingCount() throws IOException, InterruptedException {
    if (this.channel == null)
      return -1; 
    return ((Integer)this.channel.call(new LoadingCount(false))).intValue();
  }
  
  @CheckReturnValue
  public int getClassLoadingPrefetchCacheCount() throws IOException, InterruptedException {
    if (this.channel == null)
      return -1; 
    if (!this.channel.remoteCapability.supportsPrefetch())
      return -1; 
    return ((Integer)this.channel.call(new LoadingPrefetchCacheCount())).intValue();
  }
  
  @CheckReturnValue
  public int getResourceLoadingCount() throws IOException, InterruptedException {
    if (this.channel == null)
      return -1; 
    return ((Integer)this.channel.call(new LoadingCount(true))).intValue();
  }
  
  @CheckReturnValue
  public long getClassLoadingTime() throws IOException, InterruptedException {
    if (this.channel == null)
      return -1L; 
    return ((Long)this.channel.call(new LoadingTime(false))).longValue();
  }
  
  @CheckReturnValue
  public long getResourceLoadingTime() throws IOException, InterruptedException {
    if (this.channel == null)
      return -1L; 
    return ((Long)this.channel.call(new LoadingTime(true))).longValue();
  }
  
  @CheckForNull
  public String getAbsoluteRemoteFs() { return (this.channel == null) ? null : this.absoluteRemoteFs; }
  
  @Exported
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @CheckForNull
  public String getAbsoluteRemotePath() {
    if (hasPermission(CONNECT))
      return getAbsoluteRemoteFs(); 
    return null;
  }
  
  public void setChannel(@NonNull Channel channel, @CheckForNull OutputStream launchLog, @CheckForNull Channel.Listener listener) throws IOException, InterruptedException {
    if (this.channel != null)
      throw new IllegalStateException("Already connected"); 
    StreamTaskListener streamTaskListener = (launchLog != null) ? new StreamTaskListener(launchLog) : TaskListener.NULL;
    PrintStream log = streamTaskListener.getLogger();
    channel.setProperty(SlaveComputer.class, this);
    channel.addListener(new Object(this, logger, Level.FINEST, streamTaskListener));
    if (listener != null)
      channel.addListener(listener); 
    String slaveVersion = (String)channel.call(new SlaveVersion());
    log.println("Remoting version: " + slaveVersion);
    VersionNumber agentVersion = new VersionNumber(slaveVersion);
    if (agentVersion.isOlderThan(RemotingVersionInfo.getMinimumSupportedVersion())) {
      if (!ALLOW_UNSUPPORTED_REMOTING_VERSIONS) {
        streamTaskListener.fatalError("Rejecting the connection because the Remoting version is older than the minimum required version (%s). To allow the connection anyway, set the hudson.slaves.SlaveComputer.allowUnsupportedRemotingVersions system property to true.", new Object[] { RemotingVersionInfo.getMinimumSupportedVersion() });
        disconnect(new OfflineCause.LaunchFailed());
        return;
      } 
      streamTaskListener.error("The Remoting version is older than the minimum required version (%s). The connection will be allowed, but compatibility is NOT guaranteed.", new Object[] { RemotingVersionInfo.getMinimumSupportedVersion() });
    } 
    log.println("Launcher: " + getLauncher().getClass().getSimpleName());
    String communicationProtocol = (String)channel.call(new CommunicationProtocol());
    if (communicationProtocol != null)
      log.println("Communication Protocol: " + communicationProtocol); 
    boolean _isUnix = ((Boolean)channel.call(new DetectOS())).booleanValue();
    log.println(_isUnix ? Messages.Slave_UnixSlave() : Messages.Slave_WindowsSlave());
    String defaultCharsetName = (String)channel.call(new DetectDefaultCharset());
    Slave node = getNode();
    if (node == null)
      throw new IOException("Node " + this.nodeName + " has been deleted during the channel setup"); 
    String remoteFS = node.getRemoteFS();
    if (Util.isRelativePath(remoteFS)) {
      remoteFS = (String)channel.call(new AbsolutePath(remoteFS));
      log.println("NOTE: Relative remote path resolved to: " + remoteFS);
    } 
    if (_isUnix && !remoteFS.contains("/") && remoteFS.contains("\\"))
      log.println("WARNING: " + remoteFS + " looks suspiciously like Windows path. Maybe you meant " + remoteFS
          .replace('\\', '/') + "?"); 
    FilePath root = new FilePath(channel, remoteFS);
    channel.pinClassLoader(getClass().getClassLoader());
    channel.call(new SlaveInitializer(DEFAULT_RING_BUFFER_SIZE));
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      for (ComputerListener cl : ComputerListener.all())
        cl.preOnline(this, channel, root, streamTaskListener); 
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    this.offlineCause = null;
    synchronized (this.channelLock) {
      if (this.channel != null) {
        channel.close();
        throw new IllegalStateException("Already connected");
      } 
      this.isUnix = Boolean.valueOf(_isUnix);
      this.numRetryAttempt = 0;
      this.channel = channel;
      this.absoluteRemoteFs = remoteFS;
      this.defaultCharset = Charset.forName(defaultCharsetName);
      synchronized (this.statusChangeLock) {
        this.statusChangeLock.notifyAll();
      } 
    } 
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      for (ComputerListener cl : ComputerListener.all()) {
        try {
          cl.onOnline(this, streamTaskListener);
        } catch (AbortException e) {
          streamTaskListener.error(e.getMessage());
        } catch (Exception e) {
          Functions.printStackTrace(e, streamTaskListener.error(Messages.ComputerLauncher_unexpectedError()));
        } catch (Throwable e) {
          closeChannel();
          throw e;
        } 
      } 
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    log.println("Agent successfully connected and online");
    Jenkins.get().getQueue().scheduleMaintenance();
  }
  
  public Channel getChannel() { return this.channel; }
  
  public Charset getDefaultCharset() { return this.defaultCharset; }
  
  public List<LogRecord> getLogRecords() throws IOException, InterruptedException {
    if (this.channel == null)
      return Collections.emptyList(); 
    return (List)this.channel.call(new SlaveLogFetcher());
  }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void doSubmitDescription(StaplerResponse rsp, @QueryParameter String description) throws IOException {
    checkPermission(CONFIGURE);
    Slave node = getNode();
    if (node != null) {
      node.setNodeDescription(description);
    } else {
      throw new IOException("Description will be not set. The node " + this.nodeName + " does not exist (anymore).");
    } 
    rsp.sendRedirect(".");
  }
  
  @RequirePOST
  public HttpResponse doDoDisconnect(@QueryParameter String offlineMessage) {
    if (this.channel != null) {
      checkPermission(DISCONNECT);
      offlineMessage = Util.fixEmptyAndTrim(offlineMessage);
      disconnect(new OfflineCause.UserCause(User.current(), offlineMessage));
    } 
    return new HttpRedirect(".");
  }
  
  public Future<?> disconnect(OfflineCause cause) {
    super.disconnect(cause);
    return Computer.threadPoolForRemoting.submit(new Object(this));
  }
  
  @RequirePOST
  public void doLaunchSlaveAgent(StaplerRequest req, StaplerResponse rsp) throws IOException {
    checkPermission(CONNECT);
    if (this.channel != null) {
      try {
        req.getView(this, "already-launched.jelly").forward(req, rsp);
      } catch (IOException x) {
        throw x;
      } catch (Exception x) {
        throw new IOException(x);
      } 
      return;
    } 
    connect(true);
    rsp.sendRedirect("log");
  }
  
  public void tryReconnect() {
    this.numRetryAttempt++;
    if (this.numRetryAttempt < 6 || this.numRetryAttempt % 12 == 0) {
      logger.info("Attempting to reconnect " + this.nodeName);
      connect(true);
    } 
  }
  
  @Deprecated
  public Slave.JnlpJar getJnlpJars(String fileName) { return new Slave.JnlpJar(fileName); }
  
  @WebMethod(name = {"slave-agent.jnlp"})
  public HttpResponse doSlaveAgentJnlp(StaplerRequest req, StaplerResponse res) { return doJenkinsAgentJnlp(req, res); }
  
  @WebMethod(name = {"jenkins-agent.jnlp"})
  public HttpResponse doJenkinsAgentJnlp(StaplerRequest req, StaplerResponse res) { return new EncryptedSlaveAgentJnlpFile(this, "jenkins-agent.jnlp.jelly", getName(), CONNECT); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK && 
      !Jenkins.get().hasPermission(Jenkins.READ))
      return new LowPermissionResponse(this); 
    return this;
  }
  
  protected void kill() {
    super.kill();
    closeChannel();
    try {
      this.log.close();
    } catch (IOException x) {
      LOGGER.log(Level.WARNING, "Failed to close agent log", x);
    } 
    try {
      Util.deleteRecursive(getLogDir());
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Unable to delete agent logs", ex);
    } 
  }
  
  public RetentionStrategy getRetentionStrategy() {
    Slave n = getNode();
    return (n == null) ? RetentionStrategy.NOOP : n.getRetentionStrategy();
  }
  
  private void closeChannel() {
    Channel c;
    synchronized (this.channelLock) {
      c = this.channel;
      this.channel = null;
      this.absoluteRemoteFs = null;
      this.isUnix = null;
    } 
    if (c != null) {
      try {
        c.close();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to terminate channel to " + getDisplayName(), e);
      } 
      Listeners.notify(ComputerListener.class, true, l -> l.onOffline(this, this.offlineCause));
    } 
  }
  
  protected void setNode(Node node) {
    super.setNode(node);
    this.launcher = grabLauncher(node);
    if (this.constructed != null)
      if (node instanceof Slave) {
        Queue.withLock(new Object(this, node));
      } else {
        connect(false);
      }  
  }
  
  protected ComputerLauncher grabLauncher(Node node) { return ((Slave)node).getLauncher(); }
  
  @CheckReturnValue
  public String getSlaveVersion() {
    if (this.channel == null)
      return "Unknown (agent is offline)"; 
    return (String)this.channel.call(new SlaveVersion());
  }
  
  @CheckReturnValue
  public String getOSDescription() {
    if (this.channel == null)
      return "Unknown (agent is offline)"; 
    return ((Boolean)this.channel.call(new DetectOS())).booleanValue() ? "Unix" : "Windows";
  }
  
  @CheckReturnValue
  public Map<String, String> getEnvVarsFull() throws IOException, InterruptedException {
    if (this.channel == null) {
      Map<String, String> env = new TreeMap<String, String>();
      env.put("N/A", "N/A");
      return env;
    } 
    return (Map)this.channel.call(new ListFullEnvironment());
  }
  
  private static final Logger logger = Logger.getLogger(SlaveComputer.class.getName());
  
  @Deprecated
  public static VirtualChannel getChannelToMaster() { return AgentComputerUtil.getChannelToController(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.163")
  public static List<SlaveSystemInfo> getSystemInfoExtensions() { return SlaveSystemInfo.all(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean ALLOW_UNSUPPORTED_REMOTING_VERSIONS = SystemProperties.getBoolean(SlaveComputer.class.getName() + ".allowUnsupportedRemotingVersions");
  
  private static final int DEFAULT_RING_BUFFER_SIZE = SystemProperties.getInteger(hudson.util.RingBufferLogHandler.class.getName() + ".defaultSize", Integer.valueOf(256)).intValue();
  
  private static final Logger LOGGER = Logger.getLogger(SlaveComputer.class.getName());
}
