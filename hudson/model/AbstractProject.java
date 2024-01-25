package hudson.model;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.cli.declarative.CLIResolver;
import hudson.model.labels.LabelAtom;
import hudson.model.listeners.SCMPollListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskFuture;
import hudson.model.queue.SubTask;
import hudson.model.queue.SubTaskContributor;
import hudson.scm.NullSCM;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCMS;
import hudson.search.SearchIndexBuilder;
import hudson.security.Permission;
import hudson.slaves.WorkspaceList;
import hudson.tasks.BuildTrigger;
import hudson.tasks.Publisher;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.widgets.HistoryWidget;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.BlockedBecauseOfBuildInProgress;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.Uptime;
import jenkins.model.lazy.LazyBuildMixIn;
import jenkins.scm.DefaultSCMCheckoutStrategyImpl;
import jenkins.scm.SCMCheckoutStrategy;
import jenkins.scm.SCMDecisionHandler;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@BridgeMethodsAdded
public abstract class AbstractProject<P extends AbstractProject<P, R>, R extends AbstractBuild<P, R>> extends Job<P, R> implements BuildableItem, LazyBuildMixIn.LazyLoadingJob<P, R>, ParameterizedJobMixIn.ParameterizedJob<P, R> {
  private LazyBuildMixIn<P, R> buildMixIn;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected RunMap<R> builds;
  
  private String assignedNode;
  
  private static final AtomicReferenceFieldUpdater<AbstractProject, DescribableList> triggersUpdater = AtomicReferenceFieldUpdater.newUpdater(AbstractProject.class, DescribableList.class, "triggers");
  
  private boolean concurrentBuild;
  
  private String customWorkspace;
  
  protected AbstractProject(ItemGroup parent, String name) {
    super(parent, name);
    this.buildMixIn = createBuildMixIn();
    this.builds = this.buildMixIn.getRunMap();
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null && !j.getNodes().isEmpty())
      this.canRoam = true; 
  }
  
  private LazyBuildMixIn<P, R> createBuildMixIn() { return new Object(this); }
  
  public LazyBuildMixIn<P, R> getLazyBuildMixIn() { return this.buildMixIn; }
  
  public void save() throws IOException {
    super.save();
    updateTransientActions();
  }
  
  public void onCreatedFromScratch() throws IOException {
    super.onCreatedFromScratch();
    this.buildMixIn.onCreatedFromScratch();
    this.builds = this.buildMixIn.getRunMap();
    updateTransientActions();
  }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    super.onLoad(parent, name);
    if (this.buildMixIn == null)
      this.buildMixIn = createBuildMixIn(); 
    this.buildMixIn.onLoad(parent, name);
    this.builds = this.buildMixIn.getRunMap();
    triggers().setOwner(this);
    for (Trigger t : triggers()) {
      try {
        t.start(this, Items.currentlyUpdatingByXml());
      } catch (Throwable e) {
        LOGGER.log(Level.WARNING, "could not start trigger while loading project '" + getFullName() + "'", e);
      } 
    } 
    if (this.scm == null)
      this.scm = new NullSCM(); 
    if (this.transientActions == null)
      this.transientActions = new Vector(); 
    updateTransientActions();
  }
  
  @WithBridgeMethods({List.class})
  protected DescribableList<Trigger<?>, TriggerDescriptor> triggers() {
    if (this.triggers == null)
      triggersUpdater.compareAndSet(this, null, new DescribableList(this)); 
    return this.triggers;
  }
  
  @NonNull
  public EnvVars getEnvironment(@CheckForNull Node node, @NonNull TaskListener listener) throws IOException, InterruptedException {
    EnvVars env = super.getEnvironment(node, listener);
    JDK jdkTool = getJDK();
    if (jdkTool != null) {
      if (node != null)
        jdkTool = jdkTool.forNode(node, listener); 
      jdkTool.buildEnvVars(env);
    } else if (!JDK.isDefaultName(this.jdk)) {
      listener.getLogger().println("No JDK named ‘" + this.jdk + "’ found");
    } 
    return env;
  }
  
  protected void performDelete() throws IOException {
    if (supportsMakeDisabled()) {
      setDisabled(true);
      Jenkins.get().getQueue().cancel(this);
    } 
    FilePath ws = getWorkspace();
    if (ws != null) {
      Node on = getLastBuiltOn();
      getScm().processWorkspaceBeforeDeletion(this, ws, on);
    } 
    super.performDelete();
  }
  
  @Exported
  public boolean isConcurrentBuild() { return this.concurrentBuild; }
  
  public void setConcurrentBuild(boolean b) throws IOException {
    this.concurrentBuild = b;
    save();
  }
  
  @CheckForNull
  public Label getAssignedLabel() {
    if (this.canRoam)
      return null; 
    if (this.assignedNode == null)
      return Jenkins.get().getSelfLabel(); 
    return Jenkins.get().getLabel(this.assignedNode);
  }
  
  public Set<Label> getRelevantLabels() { return Collections.singleton(getAssignedLabel()); }
  
  @Exported(name = "labelExpression")
  public String getAssignedLabelString() {
    if (this.canRoam || this.assignedNode == null)
      return null; 
    try {
      Label.parseExpression(this.assignedNode);
      return this.assignedNode;
    } catch (IllegalArgumentException e) {
      return LabelAtom.escape(this.assignedNode);
    } 
  }
  
  public void setAssignedLabel(Label l) throws IOException {
    if (l == null) {
      this.canRoam = true;
      this.assignedNode = null;
    } else {
      this.canRoam = false;
      if (l == Jenkins.get().getSelfLabel()) {
        this.assignedNode = null;
      } else {
        this.assignedNode = l.getExpression();
      } 
    } 
    save();
  }
  
  public void setAssignedNode(Node l) throws IOException { setAssignedLabel(l.getSelfLabel()); }
  
  public String getPronoun() { return AlternativeUiTextProvider.get(PRONOUN, this, Messages.AbstractProject_Pronoun()); }
  
  public String getBuildNowText() { return AlternativeUiTextProvider.get(BUILD_NOW_TEXT, this, super.getBuildNowText()); }
  
  public AbstractProject<?, ?> getRootProject() {
    if (this instanceof TopLevelItem)
      return this; 
    ItemGroup p = getParent();
    if (p instanceof AbstractProject)
      return ((AbstractProject)p).getRootProject(); 
    return this;
  }
  
  @Deprecated
  public final FilePath getWorkspace() {
    AbstractBuild b = getBuildForDeprecatedMethods();
    return (b != null) ? b.getWorkspace() : null;
  }
  
  @CheckForNull
  private AbstractBuild getBuildForDeprecatedMethods() {
    Executor e = Executor.currentExecutor();
    if (e != null) {
      Queue.Executable exe = e.getCurrentExecutable();
      if (exe instanceof AbstractBuild) {
        AbstractBuild b = (AbstractBuild)exe;
        if (b.getProject() == this)
          return b; 
      } 
    } 
    return getLastBuild();
  }
  
  @CheckForNull
  public final FilePath getSomeWorkspace() {
    R b = (R)getSomeBuildWithWorkspace();
    if (b != null)
      return b.getWorkspace(); 
    for (WorkspaceBrowser browser : ExtensionList.lookup(WorkspaceBrowser.class)) {
      FilePath f = browser.getWorkspace(this);
      if (f != null)
        return f; 
    } 
    return null;
  }
  
  public final R getSomeBuildWithWorkspace() {
    for (R b = (R)getLastBuild(); b != null; b = (R)b.getPreviousBuild()) {
      FilePath ws = b.getWorkspace();
      if (ws != null)
        return b; 
    } 
    return null;
  }
  
  private R getSomeBuildWithExistingWorkspace() {
    for (R b = (R)getLastBuild(); b != null; b = (R)b.getPreviousBuild()) {
      FilePath ws = b.getWorkspace();
      if (ws != null && ws.exists())
        return b; 
    } 
    return null;
  }
  
  @Deprecated
  public FilePath getModuleRoot() {
    AbstractBuild b = getBuildForDeprecatedMethods();
    return (b != null) ? b.getModuleRoot() : null;
  }
  
  @Deprecated
  public FilePath[] getModuleRoots() {
    AbstractBuild b = getBuildForDeprecatedMethods();
    return (b != null) ? b.getModuleRoots() : null;
  }
  
  public int getQuietPeriod() { return (this.quietPeriod != null) ? this.quietPeriod.intValue() : Jenkins.get().getQuietPeriod(); }
  
  public SCMCheckoutStrategy getScmCheckoutStrategy() { return (this.scmCheckoutStrategy == null) ? new DefaultSCMCheckoutStrategyImpl() : this.scmCheckoutStrategy; }
  
  public void setScmCheckoutStrategy(SCMCheckoutStrategy scmCheckoutStrategy) throws IOException {
    this.scmCheckoutStrategy = scmCheckoutStrategy;
    save();
  }
  
  public int getScmCheckoutRetryCount() { return (this.scmCheckoutRetryCount != null) ? this.scmCheckoutRetryCount.intValue() : Jenkins.get().getScmCheckoutRetryCount(); }
  
  public boolean getHasCustomQuietPeriod() { return (this.quietPeriod != null); }
  
  public void setQuietPeriod(Integer seconds) throws IOException {
    this.quietPeriod = seconds;
    save();
  }
  
  public boolean hasCustomScmCheckoutRetryCount() { return (this.scmCheckoutRetryCount != null); }
  
  public boolean isBuildable() { return super.isBuildable(); }
  
  public boolean isConfigurable() { return true; }
  
  public boolean blockBuildWhenDownstreamBuilding() { return this.blockBuildWhenDownstreamBuilding; }
  
  public void setBlockBuildWhenDownstreamBuilding(boolean b) throws IOException {
    this.blockBuildWhenDownstreamBuilding = b;
    save();
  }
  
  public boolean blockBuildWhenUpstreamBuilding() { return this.blockBuildWhenUpstreamBuilding; }
  
  public void setBlockBuildWhenUpstreamBuilding(boolean b) throws IOException {
    this.blockBuildWhenUpstreamBuilding = b;
    save();
  }
  
  @Exported
  public boolean isDisabled() { return this.disabled; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void setDisabled(boolean disabled) throws IOException { this.disabled = disabled; }
  
  public FormValidation doCheckRetryCount(@QueryParameter String value) throws IOException, ServletException {
    if (value == null || value.trim().isEmpty())
      return FormValidation.ok(); 
    if (!value.matches("[0-9]*"))
      return FormValidation.error("Invalid retry count"); 
    return FormValidation.ok();
  }
  
  public boolean supportsMakeDisabled() { return this instanceof TopLevelItem; }
  
  public void disable() throws IOException { makeDisabled(true); }
  
  public void enable() throws IOException { makeDisabled(false); }
  
  public BallColor getIconColor() {
    if (isDisabled())
      return isBuilding() ? BallColor.DISABLED_ANIME : BallColor.DISABLED; 
    return super.getIconColor();
  }
  
  protected void updateTransientActions() throws IOException { this.transientActions = createTransientActions(); }
  
  protected List<Action> createTransientActions() {
    Vector<Action> ta = new Vector<Action>();
    for (JobProperty<? super P> p : Util.fixNull(this.properties))
      ta.addAll(p.getJobActions(this)); 
    for (TransientProjectActionFactory tpaf : TransientProjectActionFactory.all()) {
      try {
        ta.addAll(Util.fixNull(tpaf.createFor(this)));
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Could not load actions from " + tpaf + " for " + this, e);
      } 
    } 
    return ta;
  }
  
  public void addProperty(JobProperty<? super P> jobProp) throws IOException {
    super.addProperty(jobProp);
    updateTransientActions();
  }
  
  public List<ProminentProjectAction> getProminentActions() { return getActions(ProminentProjectAction.class); }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    super.doConfigSubmit(req, rsp);
    updateTransientActions();
    Jenkins.get().getQueue().scheduleMaintenance();
    Jenkins.get().rebuildDependencyGraphAsync();
  }
  
  public boolean scheduleBuild(int quietPeriod, Cause c, Action... actions) { return (scheduleBuild2(quietPeriod, c, actions) != null); }
  
  @WithBridgeMethods({Future.class})
  public QueueTaskFuture<R> scheduleBuild2(int quietPeriod, Cause c, Action... actions) { return scheduleBuild2(quietPeriod, c, Arrays.asList(actions)); }
  
  @WithBridgeMethods({Future.class})
  public QueueTaskFuture<R> scheduleBuild2(int quietPeriod, Cause c, Collection<? extends Action> actions) {
    List<Action> queueActions = new ArrayList<Action>(actions);
    if (c != null)
      queueActions.add(new CauseAction(c)); 
    return scheduleBuild2(quietPeriod, (Action[])queueActions.toArray(new Action[0]));
  }
  
  @WithBridgeMethods({Future.class})
  public QueueTaskFuture<R> scheduleBuild2(int quietPeriod) { return scheduleBuild2(quietPeriod, new Cause.LegacyCodeCause()); }
  
  @WithBridgeMethods({Future.class})
  public QueueTaskFuture<R> scheduleBuild2(int quietPeriod, Cause c) { return scheduleBuild2(quietPeriod, c, new Action[0]); }
  
  public QueueTaskFuture<R> scheduleBuild2(int quietPeriod, Action... actions) { return super.scheduleBuild2(quietPeriod, actions); }
  
  public boolean schedulePolling() {
    if (isDisabled())
      return false; 
    SCMTrigger scmt = (SCMTrigger)getTrigger(SCMTrigger.class);
    if (scmt == null)
      return false; 
    scmt.run();
    return true;
  }
  
  public boolean isInQueue() { return Jenkins.get().getQueue().contains(this); }
  
  public Queue.Item getQueueItem() { return Jenkins.get().getQueue().getItem(this); }
  
  public JDK getJDK() { return Jenkins.get().getJDK(this.jdk); }
  
  public void setJDK(JDK jdk) throws IOException {
    this.jdk = jdk.getName();
    save();
  }
  
  public BuildAuthorizationToken getAuthToken() { return this.authToken; }
  
  public RunMap<R> _getRuns() { return this.buildMixIn._getRuns(); }
  
  public void removeRun(R run) { this.buildMixIn.removeRun(run); }
  
  public R getBuild(String id) { return (R)(AbstractBuild)this.buildMixIn.getBuild(id); }
  
  public R getBuildByNumber(int n) { return (R)(AbstractBuild)this.buildMixIn.getBuildByNumber(n); }
  
  public R getFirstBuild() { return (R)(AbstractBuild)this.buildMixIn.getFirstBuild(); }
  
  @CheckForNull
  public R getLastBuild() { return (R)(AbstractBuild)this.buildMixIn.getLastBuild(); }
  
  public R getNearestBuild(int n) { return (R)(AbstractBuild)this.buildMixIn.getNearestBuild(n); }
  
  public R getNearestOldBuild(int n) { return (R)(AbstractBuild)this.buildMixIn.getNearestOldBuild(n); }
  
  protected List<R> getEstimatedDurationCandidates() { return this.buildMixIn.getEstimatedDurationCandidates(); }
  
  protected R newBuild() { return (R)(AbstractBuild)this.buildMixIn.newBuild(); }
  
  protected R loadBuild(File dir) throws IOException { return (R)(AbstractBuild)this.buildMixIn.loadBuild(dir); }
  
  @NonNull
  public List<Action> getActions() {
    List<Action> actions = new Vector<Action>(super.getActions());
    actions.addAll(this.transientActions);
    return Collections.unmodifiableList(actions);
  }
  
  public Node getLastBuiltOn() {
    AbstractBuild b = getLastBuild();
    if (b == null)
      return null; 
    return b.getBuiltOn();
  }
  
  public Object getSameNodeConstraint() { return this; }
  
  public CauseOfBlockage getCauseOfBlockage() {
    if (!isConcurrentBuild() && isLogUpdated()) {
      R lastBuild = (R)getLastBuild();
      if (lastBuild != null)
        return new BlockedBecauseOfBuildInProgress(lastBuild); 
      LOGGER.log(Level.FINE, "The last build has been deleted during the non-concurrent cause creation. The build is not blocked anymore");
    } 
    if (blockBuildWhenDownstreamBuilding()) {
      AbstractProject<?, ?> bup = getBuildingDownstream();
      if (bup != null)
        return new BecauseOfDownstreamBuildInProgress(bup); 
    } 
    if (blockBuildWhenUpstreamBuilding()) {
      AbstractProject<?, ?> bup = getBuildingUpstream();
      if (bup != null)
        return new BecauseOfUpstreamBuildInProgress(bup); 
    } 
    return null;
  }
  
  public AbstractProject getBuildingDownstream() {
    Set<Queue.Task> tasks = Jenkins.get().getQueue().getUnblockedTasks();
    for (Queue.BlockedItem item : Jenkins.get().getQueue().getBlockedItems()) {
      if (item.isCauseOfBlockageNull() || item
        .getCauseOfBlockage() instanceof BecauseOfUpstreamBuildInProgress || item
        .getCauseOfBlockage() instanceof BecauseOfDownstreamBuildInProgress)
        continue; 
      tasks.add(item.task);
    } 
    for (AbstractProject tup : getTransitiveDownstreamProjects()) {
      if (tup != this && (tup.isBuilding() || tasks.contains(tup)))
        return tup; 
    } 
    return null;
  }
  
  public AbstractProject getBuildingUpstream() {
    Set<Queue.Task> tasks = Jenkins.get().getQueue().getUnblockedTasks();
    for (Queue.BlockedItem item : Jenkins.get().getQueue().getBlockedItems()) {
      if (item.isCauseOfBlockageNull() || item
        .getCauseOfBlockage() instanceof BecauseOfUpstreamBuildInProgress || item
        .getCauseOfBlockage() instanceof BecauseOfDownstreamBuildInProgress)
        continue; 
      tasks.add(item.task);
    } 
    for (AbstractProject tup : getTransitiveUpstreamProjects()) {
      if (tup != this && (tup.isBuilding() || tasks.contains(tup)))
        return tup; 
    } 
    return null;
  }
  
  public List<SubTask> getSubTasks() {
    List<SubTask> r = new ArrayList<SubTask>();
    r.add(this);
    for (SubTaskContributor euc : SubTaskContributor.all())
      r.addAll(euc.forProject(this)); 
    for (JobProperty<? super P> p : this.properties)
      r.addAll(p.getSubTasks()); 
    return r;
  }
  
  @CheckForNull
  public R createExecutable() {
    if (isDisabled())
      return null; 
    return (R)newBuild();
  }
  
  public void checkAbortPermission() throws IOException { checkPermission(CANCEL); }
  
  public boolean hasAbortPermission() { return hasPermission(CANCEL); }
  
  @Deprecated
  public Resource getWorkspaceResource() {
    return new Resource(getFullDisplayName() + " workspace");
  }
  
  public ResourceList getResourceList() {
    Set<ResourceActivity> resourceActivities = getResourceActivities();
    List<ResourceList> resourceLists = new ArrayList<ResourceList>(1 + resourceActivities.size());
    for (ResourceActivity activity : resourceActivities) {
      if (activity != this && activity != null)
        resourceLists.add(activity.getResourceList()); 
    } 
    return ResourceList.union(resourceLists);
  }
  
  protected Set<ResourceActivity> getResourceActivities() { return Collections.emptySet(); }
  
  public boolean checkout(AbstractBuild build, Launcher launcher, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
    SCM scm = getScm();
    if (scm == null)
      return true; 
    FilePath workspace = build.getWorkspace();
    if (workspace != null) {
      workspace.mkdirs();
    } else {
      throw new AbortException("Cannot checkout SCM, workspace is not defined");
    } 
    boolean r = scm.checkout(build, launcher, workspace, listener, changelogFile);
    if (r)
      calcPollingBaseline(build, launcher, listener); 
    return r;
  }
  
  private void calcPollingBaseline(AbstractBuild build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
    SCMRevisionState baseline = (SCMRevisionState)build.getAction(SCMRevisionState.class);
    if (baseline == null) {
      try {
        baseline = getScm().calcRevisionsFromBuild(build, launcher, listener);
      } catch (AbstractMethodError e) {
        baseline = SCMRevisionState.NONE;
      } 
      if (baseline != null)
        build.addAction(baseline); 
    } 
    this.pollingBaseline = baseline;
  }
  
  @Deprecated
  public boolean pollSCMChanges(TaskListener listener) { return poll(listener).hasChanges(); }
  
  public PollingResult poll(TaskListener listener) {
    SCM scm = getScm();
    if (scm == null) {
      listener.getLogger().println(Messages.AbstractProject_NoSCM());
      return PollingResult.NO_CHANGES;
    } 
    if (!isBuildable()) {
      listener.getLogger().println(Messages.AbstractProject_Disabled());
      return PollingResult.NO_CHANGES;
    } 
    SCMDecisionHandler veto = SCMDecisionHandler.firstShouldPollVeto(this);
    if (veto != null) {
      listener.getLogger().println(Messages.AbstractProject_PollingVetoed(veto));
      return PollingResult.NO_CHANGES;
    } 
    R lb = (R)getLastBuild();
    if (lb == null) {
      listener.getLogger().println(Messages.AbstractProject_NoBuilds());
      return isInQueue() ? PollingResult.NO_CHANGES : PollingResult.BUILD_NOW;
    } 
    if (this.pollingBaseline == null) {
      R success = (R)(AbstractBuild)getLastSuccessfulBuild();
      for (R r = lb; r != null; r = (R)r.getPreviousBuild()) {
        SCMRevisionState s = (SCMRevisionState)r.getAction(SCMRevisionState.class);
        if (s != null) {
          this.pollingBaseline = s;
          break;
        } 
        if (r == success)
          break; 
      } 
    } 
    try {
      SCMPollListener.fireBeforePolling(this, listener);
      PollingResult r = _poll(listener, scm);
      SCMPollListener.firePollingSuccess(this, listener, r);
      return r;
    } catch (AbortException e) {
      listener.getLogger().println(e.getMessage());
      listener.fatalError(Messages.AbstractProject_Aborted());
      LOGGER.log(Level.FINE, "Polling " + this + " aborted", e);
      SCMPollListener.firePollingFailed(this, listener, e);
      return PollingResult.NO_CHANGES;
    } catch (IOException e) {
      Functions.printStackTrace(e, listener.fatalError(e.getMessage()));
      SCMPollListener.firePollingFailed(this, listener, e);
      return PollingResult.NO_CHANGES;
    } catch (InterruptedException e) {
      Functions.printStackTrace(e, listener.fatalError(Messages.AbstractProject_PollingABorted()));
      SCMPollListener.firePollingFailed(this, listener, e);
      return PollingResult.NO_CHANGES;
    } catch (RuntimeException|Error e) {
      SCMPollListener.firePollingFailed(this, listener, e);
      throw e;
    } 
  }
  
  private PollingResult _poll(TaskListener listener, SCM scm) throws IOException, InterruptedException {
    if (scm.requiresWorkspaceForPolling()) {
      R b = (R)getSomeBuildWithExistingWorkspace();
      if (b == null)
        b = (R)getLastBuild(); 
      FilePath ws = b.getWorkspace();
      WorkspaceOfflineReason workspaceOfflineReason = workspaceOffline(b);
      if (workspaceOfflineReason != null) {
        for (WorkspaceBrowser browser : ExtensionList.lookup(WorkspaceBrowser.class)) {
          ws = browser.getWorkspace(this);
          if (ws != null)
            return pollWithWorkspace(listener, scm, b, ws, browser.getWorkspaceList()); 
        } 
        long running = ((Uptime)Jenkins.get().getInjector().getInstance(Uptime.class)).getUptime();
        long remaining = TimeUnit.MINUTES.toMillis(10L) - running;
        if (remaining > 0L && !Functions.getIsUnitTest()) {
          listener.getLogger().print(Messages.AbstractProject_AwaitingWorkspaceToComeOnline(Long.valueOf(remaining / 1000L)));
          listener.getLogger().println(" (" + workspaceOfflineReason.name() + ")");
          return PollingResult.NO_CHANGES;
        } 
        if (workspaceOfflineReason.equals(WorkspaceOfflineReason.all_suitable_nodes_are_offline)) {
          listener.getLogger().print(Messages.AbstractProject_AwaitingWorkspaceToComeOnline(Long.valueOf(running / 1000L)));
          listener.getLogger().println(" (" + workspaceOfflineReason.name() + ")");
          return PollingResult.NO_CHANGES;
        } 
        Label label = getAssignedLabel();
        if (label != null && label.isSelfLabel()) {
          listener.getLogger().print(Messages.AbstractProject_NoWorkspace());
          listener.getLogger().println(" (" + workspaceOfflineReason.name() + ")");
          return PollingResult.NO_CHANGES;
        } 
        listener.getLogger().println((ws == null) ? 
            Messages.AbstractProject_WorkspaceOffline() : 
            Messages.AbstractProject_NoWorkspace());
        if (isInQueue()) {
          listener.getLogger().println(Messages.AbstractProject_AwaitingBuildForWorkspace());
          return PollingResult.NO_CHANGES;
        } 
        listener.getLogger().print(Messages.AbstractProject_NewBuildForWorkspace());
        listener.getLogger().println(" (" + workspaceOfflineReason.name() + ")");
        return PollingResult.BUILD_NOW;
      } 
      WorkspaceList l = b.getBuiltOn().toComputer().getWorkspaceList();
      return pollWithWorkspace(listener, scm, b, ws, l);
    } 
    LOGGER.fine("Polling SCM changes of " + getName());
    if (this.pollingBaseline == null)
      calcPollingBaseline(getLastBuild(), null, listener); 
    PollingResult r = scm.poll(this, null, null, listener, this.pollingBaseline);
    this.pollingBaseline = r.remote;
    return r;
  }
  
  private PollingResult pollWithWorkspace(TaskListener listener, SCM scm, R lb, @NonNull FilePath ws, WorkspaceList l) throws InterruptedException, IOException {
    Node node = lb.getBuiltOn();
    Launcher launcher = ws.createLauncher(listener).decorateByEnv(getEnvironment(node, listener));
    lease = l.acquire(ws, !this.concurrentBuild);
    try {
      String nodeName = (node != null) ? node.getSelfLabel().getName() : "[node_unavailable]";
      listener.getLogger().println("Polling SCM changes on " + nodeName);
      LOGGER.fine("Polling SCM changes of " + getName());
      if (this.pollingBaseline == null)
        calcPollingBaseline(lb, launcher, listener); 
      PollingResult r = scm.poll(this, launcher, ws, listener, this.pollingBaseline);
      this.pollingBaseline = r.remote;
      return r;
    } finally {
      lease.release();
    } 
  }
  
  private boolean isAllSuitableNodesOffline(R build) {
    Label label = getAssignedLabel();
    if (label != null) {
      if (label.getNodes().isEmpty())
        return false; 
      return label.isOffline();
    } 
    if (this.canRoam) {
      for (Node n : Jenkins.get().getNodes()) {
        Computer c = n.toComputer();
        if (c != null && c.isOnline() && c.isAcceptingTasks() && n.getMode() == Node.Mode.NORMAL)
          return false; 
      } 
      return (Jenkins.get().getMode() == Node.Mode.EXCLUSIVE);
    } 
    return true;
  }
  
  private WorkspaceOfflineReason workspaceOffline(R build) throws IOException, InterruptedException {
    FilePath ws = build.getWorkspace();
    Label label = getAssignedLabel();
    if (isAllSuitableNodesOffline(build)) {
      Hudson.CloudList cloudList = (label == null) ? (Jenkins.get()).clouds : label.getClouds();
      return cloudList.isEmpty() ? WorkspaceOfflineReason.all_suitable_nodes_are_offline : WorkspaceOfflineReason.use_ondemand_slave;
    } 
    if (ws == null || !ws.exists())
      return WorkspaceOfflineReason.nonexisting_workspace; 
    Node builtOn = build.getBuiltOn();
    if (builtOn == null)
      return WorkspaceOfflineReason.builton_node_gone; 
    if (builtOn.toComputer() == null)
      return WorkspaceOfflineReason.builton_node_no_executors; 
    return null;
  }
  
  public boolean hasParticipant(User user) {
    for (R build = (R)getLastBuild(); build != null; build = (R)build.getPreviousBuild()) {
      if (build.hasParticipant(user))
        return true; 
    } 
    return false;
  }
  
  @Exported
  public SCM getScm() { return this.scm; }
  
  public void setScm(SCM scm) throws IOException {
    this.scm = scm;
    save();
  }
  
  public void addTrigger(Trigger<?> trigger) throws IOException { addToList(trigger, triggers()); }
  
  public void removeTrigger(TriggerDescriptor trigger) throws IOException { removeFromList(trigger, triggers()); }
  
  protected final <T extends Describable<T>> void addToList(T item, List<T> collection) throws IOException {
    removeFromList(item.getDescriptor(), collection);
    collection.add(item);
    save();
    updateTransientActions();
  }
  
  protected final <T extends Describable<T>> void removeFromList(Descriptor<T> item, List<T> collection) throws IOException {
    Iterator<T> iCollection = collection.iterator();
    while (iCollection.hasNext()) {
      T next = (T)(Describable)iCollection.next();
      if (next.getDescriptor() == item) {
        iCollection.remove();
        save();
        updateTransientActions();
        return;
      } 
    } 
  }
  
  public Map<TriggerDescriptor, Trigger<?>> getTriggers() { return triggers().toMap(); }
  
  public <T extends Trigger> T getTrigger(Class<T> clazz) {
    for (Trigger p : triggers()) {
      if (clazz.isInstance(p))
        return (T)(Trigger)clazz.cast(p); 
    } 
    return null;
  }
  
  public final List<AbstractProject> getDownstreamProjects() { return Jenkins.get().getDependencyGraph().getDownstream(this); }
  
  @Exported(name = "downstreamProjects")
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public List<AbstractProject> getDownstreamProjectsForApi() {
    List<AbstractProject> r = new ArrayList<AbstractProject>();
    for (AbstractProject p : getDownstreamProjects()) {
      if (p.hasPermission(Item.READ))
        r.add(p); 
    } 
    return r;
  }
  
  public final List<AbstractProject> getUpstreamProjects() { return Jenkins.get().getDependencyGraph().getUpstream(this); }
  
  @Exported(name = "upstreamProjects")
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public List<AbstractProject> getUpstreamProjectsForApi() {
    List<AbstractProject> r = new ArrayList<AbstractProject>();
    for (AbstractProject p : getUpstreamProjects()) {
      if (p.hasPermission(Item.READ))
        r.add(p); 
    } 
    return r;
  }
  
  public final List<AbstractProject> getBuildTriggerUpstreamProjects() {
    ArrayList<AbstractProject> result = new ArrayList<AbstractProject>();
    for (AbstractProject<?, ?> ap : getUpstreamProjects()) {
      BuildTrigger buildTrigger = (BuildTrigger)ap.getPublishersList().get(BuildTrigger.class);
      if (buildTrigger != null && 
        buildTrigger.getChildJobs(ap).contains(this))
        result.add(ap); 
    } 
    return result;
  }
  
  public final Set<AbstractProject> getTransitiveUpstreamProjects() { return Jenkins.get().getDependencyGraph().getTransitiveUpstream(this); }
  
  public final Set<AbstractProject> getTransitiveDownstreamProjects() { return Jenkins.get().getDependencyGraph().getTransitiveDownstream(this); }
  
  public SortedMap<Integer, Fingerprint.RangeSet> getRelationship(AbstractProject that) {
    TreeMap<Integer, Fingerprint.RangeSet> r = new TreeMap<Integer, Fingerprint.RangeSet>(REVERSE_INTEGER_COMPARATOR);
    checkAndRecord(that, r, getBuilds());
    return r;
  }
  
  private void checkAndRecord(AbstractProject that, TreeMap<Integer, Fingerprint.RangeSet> r, Iterable<R> builds) {
    for (Iterator iterator = builds.iterator(); iterator.hasNext(); ) {
      R build = (R)(AbstractBuild)iterator.next();
      Fingerprint.RangeSet rs = build.getDownstreamRelationship(that);
      if (rs == null || rs.isEmpty())
        continue; 
      int n = build.getNumber();
      Fingerprint.RangeSet value = (Fingerprint.RangeSet)r.get(Integer.valueOf(n));
      if (value == null) {
        r.put(Integer.valueOf(n), rs);
        continue;
      } 
      value.add(rs);
    } 
  }
  
  protected void buildDependencyGraph(DependencyGraph graph) { triggers().buildDependencyGraph(this, graph); }
  
  protected SearchIndexBuilder makeSearchIndex() { return getParameterizedJobMixIn().extendSearchIndex(super.makeSearchIndex()); }
  
  protected HistoryWidget createHistoryWidget() { return this.buildMixIn.createHistoryWidget(); }
  
  @Deprecated
  public void doBuild(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { doBuild(req, rsp, TimeDuration.fromString(req.getParameter("delay"))); }
  
  @Deprecated
  public int getDelay(StaplerRequest req) throws ServletException {
    String delay = req.getParameter("delay");
    if (delay == null)
      return getQuietPeriod(); 
    try {
      if (delay.endsWith("sec"))
        delay = delay.substring(0, delay.length() - 3); 
      if (delay.endsWith("secs"))
        delay = delay.substring(0, delay.length() - 4); 
      return Integer.parseInt(delay);
    } catch (NumberFormatException e) {
      throw new ServletException("Invalid delay parameter value: " + delay, e);
    } 
  }
  
  @Deprecated
  public void doBuildWithParameters(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { doBuildWithParameters(req, rsp, TimeDuration.fromString(req.getParameter("delay"))); }
  
  public void doPolling(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    BuildAuthorizationToken.checkPermission(this, this.authToken, req, rsp);
    schedulePolling();
    rsp.sendRedirect(".");
  }
  
  protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    super.submit(req, rsp);
    JSONObject json = req.getSubmittedForm();
    makeDisabled(!json.optBoolean("enable"));
    this.jdk = json.optString("jdk", null);
    if (json.optBoolean("hasCustomQuietPeriod", json.has("quiet_period"))) {
      this.quietPeriod = Integer.valueOf(json.optInt("quiet_period"));
    } else {
      this.quietPeriod = null;
    } 
    if (json.optBoolean("hasCustomScmCheckoutRetryCount", json.has("scmCheckoutRetryCount"))) {
      this.scmCheckoutRetryCount = Integer.valueOf(json.optInt("scmCheckoutRetryCount"));
    } else {
      this.scmCheckoutRetryCount = null;
    } 
    this.blockBuildWhenDownstreamBuilding = json.optBoolean("blockBuildWhenDownstreamBuilding");
    this.blockBuildWhenUpstreamBuilding = json.optBoolean("blockBuildWhenUpstreamBuilding");
    if (req.hasParameter("customWorkspace.directory")) {
      LOGGER.log(Level.WARNING, "label assignment is using legacy 'customWorkspace.directory'");
      this.customWorkspace = Util.fixEmptyAndTrim(req.getParameter("customWorkspace.directory"));
    } else if (json.optBoolean("hasCustomWorkspace", json.has("customWorkspace"))) {
      this.customWorkspace = Util.fixEmptyAndTrim(json.optString("customWorkspace"));
    } else {
      this.customWorkspace = null;
    } 
    if (json.has("scmCheckoutStrategy")) {
      this.scmCheckoutStrategy = (SCMCheckoutStrategy)req.bindJSON(SCMCheckoutStrategy.class, json
          .getJSONObject("scmCheckoutStrategy"));
    } else {
      this.scmCheckoutStrategy = null;
    } 
    if (json.optBoolean("hasSlaveAffinity", json.has("label"))) {
      this.assignedNode = Util.fixEmptyAndTrim(json.optString("label"));
    } else if (req.hasParameter("_.assignedLabelString")) {
      LOGGER.log(Level.WARNING, "label assignment is using legacy '_.assignedLabelString'");
      this.assignedNode = Util.fixEmptyAndTrim(req.getParameter("_.assignedLabelString"));
    } else {
      this.assignedNode = null;
    } 
    this.canRoam = (this.assignedNode == null);
    this.keepDependencies = json.has("keepDependencies");
    this.concurrentBuild = json.optBoolean("concurrentBuild");
    this.authToken = BuildAuthorizationToken.create(req);
    setScm(SCMS.parseSCM(req, this));
    for (Trigger t : triggers())
      t.stop(); 
    this.triggers.replaceBy(buildDescribable(req, Trigger.for_(this)));
    for (Trigger t : triggers())
      t.start(this, true); 
  }
  
  @Deprecated
  protected final <T extends Describable<T>> List<T> buildDescribable(StaplerRequest req, List<? extends Descriptor<T>> descriptors, String prefix) throws Descriptor.FormException, ServletException { return buildDescribable(req, descriptors); }
  
  protected final <T extends Describable<T>> List<T> buildDescribable(StaplerRequest req, List<? extends Descriptor<T>> descriptors) throws Descriptor.FormException, ServletException {
    JSONObject data = req.getSubmittedForm();
    List<T> r = new Vector<T>();
    for (Descriptor<T> d : descriptors) {
      String safeName = d.getJsonSafeClassName();
      if (req.getParameter(safeName) != null) {
        T instance = (T)d.newInstance(req, data.getJSONObject(safeName));
        r.add(instance);
      } 
    } 
    return r;
  }
  
  public DirectoryBrowserSupport doWs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
    String title;
    checkPermission(Item.WORKSPACE);
    FilePath ws = getSomeWorkspace();
    if (ws == null || !ws.exists()) {
      req.getView(this, "noWorkspace.jelly").forward(req, rsp);
      return null;
    } 
    Computer c = ws.toComputer();
    if (c == null) {
      title = Messages.AbstractProject_WorkspaceTitle(getDisplayName());
    } else {
      title = Messages.AbstractProject_WorkspaceTitleOnComputer(getDisplayName(), c.getDisplayName());
    } 
    return new DirectoryBrowserSupport(this, ws, title, "folder.png", true);
  }
  
  @RequirePOST
  public HttpResponse doDoWipeOutWorkspace() throws IOException, ServletException, InterruptedException {
    checkPermission(Functions.isWipeOutPermissionEnabled() ? WIPEOUT : BUILD);
    R b = (R)getSomeBuildWithWorkspace();
    FilePath ws = (b != null) ? b.getWorkspace() : null;
    if (ws != null && getScm().processWorkspaceBeforeDeletion(this, ws, b.getBuiltOn())) {
      ws.deleteRecursive();
      for (WorkspaceListener wl : WorkspaceListener.all())
        wl.afterDelete(this); 
      return new HttpRedirect(".");
    } 
    return new ForwardToView(this, "wipeOutWorkspaceBlocked.jelly");
  }
  
  @CheckForNull
  public static AbstractProject findNearest(String name) { return findNearest(name, Jenkins.get()); }
  
  @CheckForNull
  public static AbstractProject findNearest(String name, ItemGroup context) { return (AbstractProject)Items.findNearest(AbstractProject.class, name, context); }
  
  private static final Comparator<Integer> REVERSE_INTEGER_COMPARATOR = Comparator.reverseOrder();
  
  private static final Logger LOGGER = Logger.getLogger(AbstractProject.class.getName());
  
  @Deprecated
  public static final Permission ABORT = CANCEL;
  
  @Deprecated
  public static final AlternativeUiTextProvider.Message<AbstractProject> BUILD_NOW_TEXT = new AlternativeUiTextProvider.Message();
  
  @CLIResolver
  public static AbstractProject resolveForCLI(@Argument(required = true, metaVar = "NAME", usage = "Job name") String name) {
    AbstractProject item = (AbstractProject)Jenkins.get().getItemByFullName(name, AbstractProject.class);
    if (item == null) {
      AbstractProject project = findNearest(name);
      throw new CmdLineException(null, (project == null) ? Messages.AbstractItem_NoSuchJobExistsWithoutSuggestion(name) : 
          Messages.AbstractItem_NoSuchJobExists(name, project.getFullName()));
    } 
    return item;
  }
  
  public String getCustomWorkspace() { return this.customWorkspace; }
  
  public void setCustomWorkspace(String customWorkspace) throws IOException {
    this.customWorkspace = Util.fixEmptyAndTrim(customWorkspace);
    save();
  }
  
  public abstract DescribableList<Publisher, Descriptor<Publisher>> getPublishersList();
  
  protected abstract Class<R> getBuildClass();
  
  public abstract boolean isFingerprintConfigured();
}
