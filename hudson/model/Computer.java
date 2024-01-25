package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Util;
import hudson.cli.declarative.CLIResolver;
import hudson.console.AnnotatedLargeText;
import hudson.init.Initializer;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.WorkUnit;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.slaves.ComputerListener;
import hudson.slaves.Messages;
import hudson.slaves.NodeProperty;
import hudson.slaves.OfflineCause;
import hudson.slaves.RetentionStrategy;
import hudson.slaves.WorkspaceList;
import hudson.triggers.SafeTimerTask;
import hudson.util.DaemonThreadFactory;
import hudson.util.EditDistance;
import hudson.util.ExceptionCatchingThreadFactory;
import hudson.util.Futures;
import hudson.util.IOUtils;
import hudson.util.NamingThreadFactory;
import hudson.util.RemotingDiagnostics;
import hudson.util.RunList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.security.ImpersonatingExecutorService;
import jenkins.security.stapler.StaplerDispatchable;
import jenkins.util.ContextResettingExecutorService;
import jenkins.util.ErrorLoggingExecutorService;
import jenkins.util.Listeners;
import jenkins.util.SystemProperties;
import jenkins.widgets.HasWidgets;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@ExportedBean
public abstract class Computer extends Actionable implements AccessControlled, ExecutorListener, DescriptorByNameOwner, StaplerProxy, HasWidgets {
  private final CopyOnWriteArrayList<Executor> executors;
  
  private final CopyOnWriteArrayList<OneOffExecutor> oneOffExecutors;
  
  private int numExecutors;
  
  private long connectTime;
  
  private boolean temporarilyOffline;
  
  protected String nodeName;
  
  private final WorkspaceList workspaceList;
  
  protected List<Action> transientActions;
  
  protected final Object statusChangeLock;
  
  private final Object logDirLock;
  
  private final List<TerminationRequest> terminatedBy;
  
  public void recordTermination() {
    StaplerRequest request = Stapler.getCurrentRequest();
    if (request != null) {
      this.terminatedBy.add(new TerminationRequest(
            String.format("Termination requested at %s by %s [id=%d] from HTTP request for %s", new Object[] { new Date(), Thread.currentThread(), 
                Long.valueOf(Thread.currentThread().getId()), request
                .getRequestURL() })));
    } else {
      this.terminatedBy.add(new TerminationRequest(
            String.format("Termination requested at %s by %s [id=%d]", new Object[] { new Date(), Thread.currentThread(), 
                Long.valueOf(Thread.currentThread().getId()) })));
    } 
  }
  
  public List<TerminationRequest> getTerminatedBy() { return new ArrayList(this.terminatedBy); }
  
  protected Computer(Node node) {
    this.executors = new CopyOnWriteArrayList();
    this.oneOffExecutors = new CopyOnWriteArrayList();
    this.connectTime = 0L;
    this.workspaceList = new WorkspaceList();
    this.statusChangeLock = new Object();
    this.logDirLock = new Object();
    this.terminatedBy = Collections.synchronizedList(new ArrayList());
    setNode(node);
  }
  
  public List<ComputerPanelBox> getComputerPanelBoxs() { return ComputerPanelBox.all(this); }
  
  @NonNull
  public List<Action> getActions() {
    List<Action> result = new ArrayList<Action>(super.getActions());
    synchronized (this) {
      if (this.transientActions == null)
        this.transientActions = TransientComputerActionFactory.createAllFor(this); 
      result.addAll(this.transientActions);
    } 
    return Collections.unmodifiableList(result);
  }
  
  public void addAction(@NonNull Action a) {
    if (a == null)
      throw new IllegalArgumentException("Action must be non-null"); 
    super.getActions().add(a);
  }
  
  @NonNull
  public File getLogFile() { return new File(getLogDir(), "slave.log"); }
  
  @NonNull
  protected File getLogDir() {
    File dir = new File(SafeTimerTask.getLogsRoot(), "slaves/" + this.nodeName);
    synchronized (this.logDirLock) {
      try {
        IOUtils.mkdirs(dir);
      } catch (IOException x) {
        LOGGER.log(Level.SEVERE, "Failed to create agent log directory " + dir, x);
      } 
    } 
    return dir;
  }
  
  public WorkspaceList getWorkspaceList() { return this.workspaceList; }
  
  public String getLog() throws IOException { return Util.loadFile(getLogFile(), Charset.defaultCharset()); }
  
  public AnnotatedLargeText<Computer> getLogText() {
    checkAnyPermission(new Permission[] { CONNECT, EXTENDED_READ });
    return new AnnotatedLargeText(getLogFile(), Charset.defaultCharset(), false, this);
  }
  
  @NonNull
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  @Exported
  public OfflineCause getOfflineCause() { return this.offlineCause; }
  
  @Exported
  public String getOfflineCauseReason() throws IOException {
    if (this.offlineCause == null)
      return ""; 
    String gsub_base = Messages.SlaveComputer_DisconnectedBy("", "");
    String gsub1 = "^" + gsub_base + "[\\w\\W]* \\: ";
    String gsub2 = "^" + gsub_base + "[\\w\\W]*";
    String newString = this.offlineCause.toString().replaceAll(gsub1, "");
    return newString.replaceAll(gsub2, "");
  }
  
  @Deprecated
  public final void launch() { connect(true); }
  
  public final Future<?> connect(boolean forceReconnect) {
    this.connectTime = System.currentTimeMillis();
    return _connect(forceReconnect);
  }
  
  @Deprecated
  public void cliConnect(boolean force) throws ExecutionException, InterruptedException {
    checkPermission(CONNECT);
    connect(force).get();
  }
  
  public final long getConnectTime() { return this.connectTime; }
  
  public Future<?> disconnect(OfflineCause cause) {
    recordTermination();
    this.offlineCause = cause;
    if (Util.isOverridden(Computer.class, getClass(), "disconnect", new Class[0]))
      return disconnect(); 
    this.connectTime = 0L;
    return Futures.precomputed(null);
  }
  
  @Deprecated
  public Future<?> disconnect() {
    recordTermination();
    if (Util.isOverridden(Computer.class, getClass(), "disconnect", new Class[] { OfflineCause.class }))
      return disconnect(null); 
    this.connectTime = 0L;
    return Futures.precomputed(null);
  }
  
  @Deprecated
  public void cliDisconnect(String cause) throws ExecutionException, InterruptedException {
    checkPermission(DISCONNECT);
    disconnect(new OfflineCause.ByCLI(cause)).get();
  }
  
  @Deprecated
  public void cliOffline(String cause) throws ExecutionException, InterruptedException {
    checkPermission(DISCONNECT);
    setTemporarilyOffline(true, new OfflineCause.ByCLI(cause));
  }
  
  @Deprecated
  public void cliOnline() {
    checkPermission(CONNECT);
    setTemporarilyOffline(false, null);
  }
  
  @Exported
  public int getNumExecutors() { return this.numExecutors; }
  
  @NonNull
  public String getName() throws IOException { return (this.nodeName != null) ? this.nodeName : ""; }
  
  @CheckForNull
  public Node getNode() {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null)
      return null; 
    if (this.nodeName == null)
      return j; 
    return j.getNode(this.nodeName);
  }
  
  @Exported
  public LoadStatistics getLoadStatistics() { return (LabelAtom.get((this.nodeName != null) ? this.nodeName : Jenkins.get().getSelfLabel().toString())).loadStatistics; }
  
  public BuildTimelineWidget getTimeline() { return new BuildTimelineWidget(getBuilds()); }
  
  @Exported
  public boolean isOffline() { return (this.temporarilyOffline || getChannel() == null); }
  
  public final boolean isOnline() { return !isOffline(); }
  
  @Exported
  public boolean isManualLaunchAllowed() { return getRetentionStrategy().isManualLaunchAllowed(this); }
  
  @Exported
  @Deprecated
  public boolean isJnlpAgent() { return false; }
  
  @Exported
  public boolean isLaunchSupported() { return true; }
  
  @Exported
  @Deprecated
  public boolean isTemporarilyOffline() { return this.temporarilyOffline; }
  
  @Deprecated
  public void setTemporarilyOffline(boolean temporarilyOffline) throws ExecutionException, InterruptedException { setTemporarilyOffline(temporarilyOffline, null); }
  
  public void setTemporarilyOffline(boolean temporarilyOffline, OfflineCause cause) {
    this.offlineCause = temporarilyOffline ? cause : null;
    this.temporarilyOffline = temporarilyOffline;
    Node node = getNode();
    if (node != null)
      node.setTemporaryOfflineCause(this.offlineCause); 
    synchronized (this.statusChangeLock) {
      this.statusChangeLock.notifyAll();
    } 
    if (temporarilyOffline) {
      Listeners.notify(ComputerListener.class, false, l -> l.onTemporarilyOffline(this, cause));
    } else {
      Listeners.notify(ComputerListener.class, false, l -> l.onTemporarilyOnline(this));
    } 
  }
  
  @Exported
  public String getIcon() throws IOException {
    if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
      return "symbol-computer-disconnected"; 
    if (isOffline() || !isAcceptingTasks())
      return "symbol-computer-offline"; 
    return "symbol-computer";
  }
  
  @Exported
  public String getIconClassName() throws IOException {
    if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
      return "symbol-computer-disconnected"; 
    if (isOffline() || !isAcceptingTasks())
      return "symbol-computer-offline"; 
    return "symbol-computer";
  }
  
  public String getIconAltText() throws IOException {
    if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
      return "[temporarily offline by user]"; 
    if (isOffline() || !isAcceptingTasks())
      return "[offline]"; 
    return "[online]";
  }
  
  @Exported
  @NonNull
  public String getDisplayName() throws IOException { return this.nodeName; }
  
  public String getCaption() throws IOException { return Messages.Computer_Caption(this.nodeName); }
  
  public String getUrl() throws IOException { return "computer/" + Util.fullEncode(getName()) + "/"; }
  
  @Exported
  public Set<LabelAtom> getAssignedLabels() {
    Node node = getNode();
    return (node != null) ? node.getAssignedLabels() : Collections.emptySet();
  }
  
  public List<AbstractProject> getTiedJobs() {
    Node node = getNode();
    return (node != null) ? node.getSelfLabel().getTiedJobs() : Collections.emptyList();
  }
  
  public RunList getBuilds() { return RunList.fromJobs(Jenkins.get().allItems(Job.class)).node(getNode()); }
  
  protected void setNode(Node node) {
    assert node != null;
    if (node instanceof Slave) {
      this.nodeName = node.getNodeName();
    } else {
      this.nodeName = null;
    } 
    setNumExecutors(node.getNumExecutors());
    if (this.temporarilyOffline)
      node.setTemporaryOfflineCause(this.offlineCause); 
  }
  
  protected void kill() { setNumExecutors(0); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @GuardedBy("hudson.model.Queue.lock")
  void inflictMortalWound() { setNumExecutors(0); }
  
  protected void onRemoved() {}
  
  @GuardedBy("hudson.model.Queue.lock")
  private void setNumExecutors(int n) {
    this.numExecutors = n;
    int diff = this.executors.size() - n;
    if (diff > 0)
      Queue.withLock(() -> {
            for (Executor e : this.executors) {
              if (e.isIdle())
                e.interrupt(); 
            } 
          }); 
    if (diff < 0)
      addNewExecutorIfNecessary(); 
  }
  
  private void addNewExecutorIfNecessary() {
    if (Jenkins.getInstanceOrNull() == null)
      return; 
    Set<Integer> availableNumbers = new HashSet<Integer>();
    for (int i = 0; i < this.numExecutors; i++)
      availableNumbers.add(Integer.valueOf(i)); 
    for (Executor executor : this.executors)
      availableNumbers.remove(Integer.valueOf(executor.getNumber())); 
    for (Integer number : availableNumbers) {
      if (this.executors.size() < this.numExecutors) {
        Executor e = new Executor(this, number.intValue());
        this.executors.add(e);
      } 
    } 
  }
  
  public int countIdle() {
    int n = 0;
    for (Executor e : this.executors) {
      if (e.isIdle())
        n++; 
    } 
    return n;
  }
  
  public final int countBusy() { return countExecutors() - countIdle(); }
  
  public final int countExecutors() { return this.executors.size(); }
  
  @Exported
  @StaplerDispatchable
  public List<Executor> getExecutors() { return new ArrayList(this.executors); }
  
  @Exported
  @StaplerDispatchable
  public List<OneOffExecutor> getOneOffExecutors() { return new ArrayList(this.oneOffExecutors); }
  
  public List<Executor> getAllExecutors() {
    List<Executor> result = new ArrayList<Executor>(this.executors.size() + this.oneOffExecutors.size());
    result.addAll(this.executors);
    result.addAll(this.oneOffExecutors);
    return result;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<DisplayExecutor> getDisplayExecutors() {
    List<DisplayExecutor> result = new ArrayList<DisplayExecutor>(this.executors.size() + this.oneOffExecutors.size());
    int index = 0;
    for (Executor e : this.executors) {
      if (e.isDisplayCell())
        result.add(new DisplayExecutor(Integer.toString(index + 1), String.format("executors/%d", new Object[] { Integer.valueOf(index) }), e)); 
      index++;
    } 
    index = 0;
    for (OneOffExecutor e : this.oneOffExecutors) {
      if (e.isDisplayCell())
        result.add(new DisplayExecutor("", String.format("oneOffExecutors/%d", new Object[] { Integer.valueOf(index) }), e)); 
      index++;
    } 
    return result;
  }
  
  @Exported
  public final boolean isIdle() {
    if (!this.oneOffExecutors.isEmpty())
      return false; 
    for (Executor e : this.executors) {
      if (!e.isIdle())
        return false; 
    } 
    return true;
  }
  
  public final boolean isPartiallyIdle() {
    for (Executor e : this.executors) {
      if (e.isIdle())
        return true; 
    } 
    return false;
  }
  
  public final long getIdleStartMilliseconds() {
    long firstIdle = Float.MIN_VALUE;
    for (Executor e : this.oneOffExecutors)
      firstIdle = Math.max(firstIdle, e.getIdleStartMilliseconds()); 
    for (Executor e : this.executors)
      firstIdle = Math.max(firstIdle, e.getIdleStartMilliseconds()); 
    return firstIdle;
  }
  
  public final long getDemandStartMilliseconds() {
    long firstDemand = Float.MAX_VALUE;
    for (Queue.BuildableItem item : Jenkins.get().getQueue().getBuildableItems(this))
      firstDemand = Math.min(item.buildableStartMilliseconds, firstDemand); 
    return firstDemand;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @Exported
  @NonNull
  public String getDescription() throws IOException {
    Node node = getNode();
    return (node != null) ? node.getNodeDescription() : "";
  }
  
  protected void removeExecutor(Executor e) {
    Runnable task = () -> {
        synchronized (this) {
          this.executors.remove(e);
          this.oneOffExecutors.remove(e);
          addNewExecutorIfNecessary();
          if (!isAlive()) {
            Jenkins jenkins = Jenkins.getInstanceOrNull();
            if (jenkins != null)
              jenkins.removeComputer(this); 
          } 
        } 
      };
    if (!Queue.tryWithLock(task))
      threadPoolForRemoting.submit(Queue.wrapWithLock(task)); 
  }
  
  protected boolean isAlive() {
    for (Executor e : this.executors) {
      if (e.isActive())
        return true; 
    } 
    return false;
  }
  
  public void interrupt() {
    Queue.withLock(() -> {
          for (Executor e : this.executors)
            e.interruptForShutdown(); 
        });
  }
  
  public String getSearchUrl() throws IOException { return getUrl(); }
  
  @Exported(inline = true)
  public Map<String, Object> getMonitorData() {
    Map<String, Object> r = new HashMap<String, Object>();
    if (hasPermission(CONNECT))
      for (NodeMonitor monitor : NodeMonitor.getAll())
        r.put(monitor.getClass().getName(), monitor.data(this));  
    return r;
  }
  
  public Map<Object, Object> getSystemProperties() throws IOException, InterruptedException { return RemotingDiagnostics.getSystemProperties(getChannel()); }
  
  @Deprecated
  public Map<String, String> getEnvVars() throws IOException, InterruptedException { return getEnvironment(); }
  
  public EnvVars getEnvironment() throws IOException, InterruptedException {
    EnvVars cachedEnvironment = this.cachedEnvironment;
    if (cachedEnvironment != null)
      return new EnvVars(cachedEnvironment); 
    cachedEnvironment = EnvVars.getRemote(getChannel());
    this.cachedEnvironment = cachedEnvironment;
    return new EnvVars(cachedEnvironment);
  }
  
  @NonNull
  public EnvVars buildEnvironment(@NonNull TaskListener listener) throws IOException, InterruptedException {
    EnvVars env = new EnvVars();
    Node node = getNode();
    if (node == null)
      return env; 
    for (NodeProperty nodeProperty : Jenkins.get().getGlobalNodeProperties())
      nodeProperty.buildEnvVars(env, listener); 
    for (NodeProperty nodeProperty : node.getNodeProperties())
      nodeProperty.buildEnvVars(env, listener); 
    String rootUrl = Jenkins.get().getRootUrl();
    if (rootUrl != null) {
      env.put("HUDSON_URL", rootUrl);
      env.put("JENKINS_URL", rootUrl);
    } 
    return env;
  }
  
  public Map<String, String> getThreadDump() throws IOException, InterruptedException { return RemotingDiagnostics.getThreadDump(getChannel()); }
  
  public RemotingDiagnostics.HeapDump getHeapDump() throws IOException { return new RemotingDiagnostics.HeapDump(this, getChannel()); }
  
  public String getHostName() throws IOException {
    if (this.hostNameCached)
      return this.cachedHostName; 
    VirtualChannel channel = getChannel();
    if (channel == null)
      return null; 
    for (String address : (List)channel.call(new ListPossibleNames())) {
      try {
        InetAddress ia = InetAddress.getByName(address);
        if (!(ia instanceof java.net.Inet4Address)) {
          LOGGER.log(Level.FINE, "{0} is not an IPv4 address", address);
          continue;
        } 
        if (!ComputerPinger.checkIsReachable(ia, 3)) {
          LOGGER.log(Level.FINE, "{0} didn't respond to ping", address);
          continue;
        } 
        this.cachedHostName = ia.getCanonicalHostName();
        this.hostNameCached = true;
        return this.cachedHostName;
      } catch (IOException e) {
        LogRecord lr = new LogRecord(Level.FINE, "Failed to parse {0}");
        lr.setThrown(e);
        lr.setParameters(new Object[] { address });
        LOGGER.log(lr);
      } 
    } 
    this.cachedHostName = (String)channel.call(new GetFallbackName());
    this.hostNameCached = true;
    return this.cachedHostName;
  }
  
  final void startFlyWeightTask(WorkUnit p) {
    OneOffExecutor e = new OneOffExecutor(this);
    e.start(p);
    this.oneOffExecutors.add(e);
  }
  
  final void remove(OneOffExecutor e) { this.oneOffExecutors.remove(e); }
  
  public static final ExecutorService threadPoolForRemoting = new ContextResettingExecutorService(new ImpersonatingExecutorService(new ErrorLoggingExecutorService(

          
          Executors.newCachedThreadPool(new ExceptionCatchingThreadFactory(new NamingThreadFactory(new DaemonThreadFactory(), "Computer.threadPoolForRemoting")))), ACL.SYSTEM2));
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void doRssAll(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (all builds)", getUrl(), getBuilds()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void doRssFailed(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (failed builds)", getUrl(), getBuilds().failureOnly()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void doRssLatest(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    List<Run> lastBuilds = new ArrayList<Run>();
    for (AbstractProject<?, ?> p : Jenkins.get().allItems(AbstractProject.class)) {
      if (p.getLastBuild() != null)
        for (AbstractBuild<?, ?> b = p.getLastBuild(); b != null; b = b.getPreviousBuild()) {
          if (b.getBuiltOn() == getNode()) {
            lastBuilds.add(b);
            break;
          } 
        }  
    } 
    RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (latest builds)", getUrl(), RunList.fromRuns(lastBuilds));
  }
  
  @RequirePOST
  public HttpResponse doToggleOffline(@QueryParameter String offlineMessage) throws IOException, ServletException {
    if (!this.temporarilyOffline) {
      checkPermission(DISCONNECT);
      offlineMessage = Util.fixEmptyAndTrim(offlineMessage);
      setTemporarilyOffline(!this.temporarilyOffline, new OfflineCause.UserCause(
            User.current(), offlineMessage));
    } else {
      checkPermission(CONNECT);
      setTemporarilyOffline(!this.temporarilyOffline, null);
    } 
    return HttpResponses.redirectToDot();
  }
  
  @RequirePOST
  public HttpResponse doChangeOfflineCause(@QueryParameter String offlineMessage) throws IOException, ServletException {
    checkPermission(DISCONNECT);
    offlineMessage = Util.fixEmptyAndTrim(offlineMessage);
    setTemporarilyOffline(true, new OfflineCause.UserCause(
          User.current(), offlineMessage));
    return HttpResponses.redirectToDot();
  }
  
  public Api getApi() { return new Api(this); }
  
  public void doDumpExportTable(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(Jenkins.ADMINISTER);
    rsp.setContentType("text/plain");
    PrintWriter w = new PrintWriter(rsp.getCompressedWriter(req));
    try {
      VirtualChannel vc = getChannel();
      if (vc instanceof Channel) {
        w.println("Controller to agent");
        ((Channel)vc).dumpExportTable(w);
        w.flush();
        w.println("\n\n\nAgent to controller");
        w.print((String)vc.call(new DumpExportTableTask()));
      } else {
        w.println(Messages.Computer_BadChannel());
      } 
      w.close();
    } catch (Throwable throwable) {
      try {
        w.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { _doScript(req, rsp, "_script.jelly"); }
  
  public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { _doScript(req, rsp, "_scriptText.jelly"); }
  
  protected void _doScript(StaplerRequest req, StaplerResponse rsp, String view) throws IOException, ServletException { Jenkins._doScript(req, rsp, req.getView(this, view), getChannel(), getACL()); }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    String proposedName = Util.fixEmptyAndTrim(req.getSubmittedForm().getString("name"));
    Jenkins.checkGoodName(proposedName);
    Node node = getNode();
    if (node == null)
      throw new ServletException("No such node " + this.nodeName); 
    if (!proposedName.equals(this.nodeName) && 
      Jenkins.get().getNode(proposedName) != null)
      throw new Descriptor.FormException(Messages.ComputerSet_SlaveAlreadyExists(proposedName), "name"); 
    String nExecutors = req.getSubmittedForm().getString("numExecutors");
    if (StringUtils.isBlank(nExecutors) || Integer.parseInt(nExecutors) <= 0)
      throw new Descriptor.FormException(Messages.Slave_InvalidConfig_Executors(this.nodeName), "numExecutors"); 
    Node result = node.reconfigure(req, req.getSubmittedForm());
    Jenkins.get().getNodesObject().replaceNode(getNode(), result);
    rsp.sendRedirect2("../" + result.getNodeName() + "/");
  }
  
  @WebMethod(name = {"config.xml"})
  public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (req.getMethod().equals("GET")) {
      checkPermission(EXTENDED_READ);
      rsp.setContentType("application/xml");
      Node node = getNode();
      if (node == null)
        throw HttpResponses.notFound(); 
      Jenkins.XSTREAM2.toXMLUTF8(node, rsp.getOutputStream());
      return;
    } 
    if (req.getMethod().equals("POST")) {
      updateByXml(req.getInputStream());
      return;
    } 
    rsp.sendError(400);
  }
  
  public void updateByXml(InputStream source) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    Node previous = getNode();
    if (previous == null)
      throw HttpResponses.notFound(); 
    Node result = (Node)Jenkins.XSTREAM2.fromXML(source);
    if (previous.getClass() != result.getClass())
      throw HttpResponses.errorWithoutStack(400, "Node types do not match"); 
    Jenkins.get().getNodesObject().replaceNode(previous, result);
  }
  
  @RequirePOST
  public HttpResponse doDoDelete() throws IOException {
    checkPermission(DELETE);
    Node node = getNode();
    if (node != null) {
      Jenkins.get().removeNode(node);
    } else {
      Jenkins jenkins = Jenkins.get();
      jenkins.removeComputer(this);
    } 
    return new HttpRedirect("..");
  }
  
  public void waitUntilOnline() {
    synchronized (this.statusChangeLock) {
      while (!isOnline())
        this.statusChangeLock.wait(1000L); 
    } 
  }
  
  public void waitUntilOffline() {
    synchronized (this.statusChangeLock) {
      while (!isOffline())
        this.statusChangeLock.wait(1000L); 
    } 
  }
  
  public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { getLogText().doProgressText(req, rsp); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.READ); 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(Computer.class.getName() + ".skipPermissionCheck");
  
  @Nullable
  public static Computer currentComputer() {
    e = Executor.currentExecutor();
    return (e != null) ? e.getOwner() : null;
  }
  
  @OverrideMustInvoke
  public boolean isAcceptingTasks() {
    Node node = getNode();
    return (getRetentionStrategy().isAcceptingTasks(this) && (node == null || node.isAcceptingTasks()));
  }
  
  @CLIResolver
  public static Computer resolveForCLI(@Argument(required = true, metaVar = "NAME", usage = "Agent name, or empty string for built-in node") String name) throws CmdLineException {
    Jenkins h = Jenkins.get();
    Computer item = h.getComputer(name);
    if (item == null) {
      List<String> names = ComputerSet.getComputerNames();
      String adv = EditDistance.findNearest(name, names);
      throw new IllegalArgumentException((adv == null) ? 
          Messages.Computer_NoSuchSlaveExistsWithoutAdvice(name) : 
          Messages.Computer_NoSuchSlaveExists(name, adv));
    } 
    return item;
  }
  
  @Initializer
  public static void relocateOldLogs() { relocateOldLogs(Jenkins.get().getRootDir()); }
  
  static void relocateOldLogs(File dir) {
    Pattern logfile = Pattern.compile("slave-(.*)\\.log(\\.[0-9]+)?");
    File[] logfiles = dir.listFiles((dir1, name) -> logfile.matcher(name).matches());
    if (logfiles == null)
      return; 
    for (File f : logfiles) {
      Matcher m = logfile.matcher(f.getName());
      if (m.matches()) {
        File newLocation = new File(dir, "logs/slaves/" + m.group(1) + "/slave.log" + Util.fixNull(m.group(2)));
        try {
          Util.createDirectories(newLocation.getParentFile().toPath(), new java.nio.file.attribute.FileAttribute[0]);
          Files.move(f.toPath(), newLocation.toPath(), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
          LOGGER.log(Level.INFO, "Relocated log file {0} to {1}", new Object[] { f.getPath(), newLocation.getPath() });
        } catch (IOException|java.nio.file.InvalidPathException e) {
          LOGGER.log(Level.WARNING, e, () -> "Cannot relocate log file " + f.getPath() + " to " + newLocation.getPath());
        } 
      } else {
        assert false;
      } 
    } 
  }
  
  public static final PermissionGroup PERMISSIONS = new PermissionGroup(Computer.class, Messages._Computer_Permissions_Title());
  
  public static final Permission CONFIGURE = new Permission(PERMISSIONS, "Configure", 


      
      Messages._Computer_ConfigurePermission_Description(), Permission.CONFIGURE, PermissionScope.COMPUTER);
  
  public static final Permission EXTENDED_READ = new Permission(PERMISSIONS, "ExtendedRead", 


      
      Messages._Computer_ExtendedReadPermission_Description(), CONFIGURE, 
      
      SystemProperties.getBoolean("hudson.security.ExtendedReadPermission"), new PermissionScope[] { PermissionScope.COMPUTER });
  
  public static final Permission DELETE = new Permission(PERMISSIONS, "Delete", 


      
      Messages._Computer_DeletePermission_Description(), Permission.DELETE, PermissionScope.COMPUTER);
  
  public static final Permission CREATE = new Permission(PERMISSIONS, "Create", 


      
      Messages._Computer_CreatePermission_Description(), Permission.CREATE, PermissionScope.JENKINS);
  
  public static final Permission DISCONNECT = new Permission(PERMISSIONS, "Disconnect", 


      
      Messages._Computer_DisconnectPermission_Description(), Jenkins.ADMINISTER, PermissionScope.COMPUTER);
  
  public static final Permission CONNECT = new Permission(PERMISSIONS, "Connect", 


      
      Messages._Computer_ConnectPermission_Description(), DISCONNECT, PermissionScope.COMPUTER);
  
  public static final Permission BUILD = new Permission(PERMISSIONS, "Build", 


      
      Messages._Computer_BuildPermission_Description(), Permission.WRITE, PermissionScope.COMPUTER);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final Permission[] EXTENDED_READ_AND_CONNECT = { EXTENDED_READ, CONNECT };
  
  private static final Logger LOGGER = Logger.getLogger(Computer.class.getName());
  
  @Nullable
  public abstract VirtualChannel getChannel();
  
  public abstract Charset getDefaultCharset();
  
  public abstract List<LogRecord> getLogRecords() throws IOException, InterruptedException;
  
  public abstract void doLaunchSlaveAgent(StaplerRequest paramStaplerRequest, StaplerResponse paramStaplerResponse) throws IOException, ServletException;
  
  protected abstract Future<?> _connect(boolean paramBoolean);
  
  @CheckForNull
  public abstract Boolean isUnix();
  
  public abstract boolean isConnecting();
  
  public abstract RetentionStrategy getRetentionStrategy();
}
