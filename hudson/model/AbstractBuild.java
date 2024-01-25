package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.queue.SubTask;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.NullChangeLogParser;
import hudson.slaves.WorkspaceList;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Fingerprinter;
import hudson.util.HttpResponses;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.model.lazy.BuildReference;
import jenkins.model.lazy.LazyBuildMixIn;
import jenkins.scm.RunWithSCM;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;

public abstract class AbstractBuild<P extends AbstractProject<P, R>, R extends AbstractBuild<P, R>> extends Run<P, R> implements Queue.Executable, LazyBuildMixIn.LazyLoadingRun<P, R>, RunWithSCM<P, R> {
  private static final boolean upstreamCulprits = SystemProperties.getBoolean("hudson.upstreamCulprits");
  
  private String builtOn;
  
  private String workspace;
  
  private String hudsonVersion;
  
  private ChangeLogParser scm;
  
  protected List<Environment> buildEnvironments;
  
  private final LazyBuildMixIn.RunMixIn<P, R> runMixIn = new Object(this);
  
  private Object changeSetLock;
  
  protected AbstractBuild(P job) throws IOException {
    super(job);
    this.changeSetLock = new Object();
  }
  
  protected AbstractBuild(P job, Calendar timestamp) {
    super(job, timestamp);
    this.changeSetLock = new Object();
  }
  
  protected AbstractBuild(P project, File buildDir) throws IOException {
    super(project, buildDir);
    this.changeSetLock = new Object();
  }
  
  public final P getProject() { return (P)(AbstractProject)getParent(); }
  
  public final LazyBuildMixIn.RunMixIn<P, R> getRunMixIn() { return this.runMixIn; }
  
  @NonNull
  protected final BuildReference<R> createReference() { return getRunMixIn().createReference(); }
  
  protected final void dropLinks() { getRunMixIn().dropLinks(); }
  
  public R getPreviousBuild() { return (R)(AbstractBuild)getRunMixIn().getPreviousBuild(); }
  
  public R getNextBuild() { return (R)(AbstractBuild)getRunMixIn().getNextBuild(); }
  
  @CheckForNull
  public Node getBuiltOn() {
    if (this.builtOn == null || this.builtOn.isEmpty())
      return Jenkins.get(); 
    return Jenkins.get().getNode(this.builtOn);
  }
  
  @Exported(name = "builtOn")
  public String getBuiltOnStr() { return this.builtOn; }
  
  protected void setBuiltOnStr(String builtOn) { this.builtOn = builtOn; }
  
  public AbstractBuild<?, ?> getRootBuild() { return this; }
  
  public Queue.Executable getParentExecutable() {
    AbstractBuild<?, ?> rootBuild = getRootBuild();
    return (rootBuild != this) ? rootBuild : null;
  }
  
  @Deprecated(since = "2.364")
  public String getUpUrl() {
    return Functions.getNearestAncestorUrl(Stapler.getCurrentRequest(), getParent()) + "/";
  }
  
  @CheckForNull
  public final FilePath getWorkspace() {
    if (this.workspace == null)
      return null; 
    Node n = getBuiltOn();
    if (n == null)
      return null; 
    return n.createPath(this.workspace);
  }
  
  protected void setWorkspace(@NonNull FilePath ws) { this.workspace = ws.getRemote(); }
  
  public final FilePath getModuleRoot() {
    FilePath ws = getWorkspace();
    if (ws == null)
      return null; 
    return ((AbstractProject)getParent()).getScm().getModuleRoot(ws, this);
  }
  
  public FilePath[] getModuleRoots() {
    FilePath ws = getWorkspace();
    if (ws == null)
      return null; 
    return ((AbstractProject)getParent()).getScm().getModuleRoots(ws, this);
  }
  
  @CheckForNull
  public Set<String> getCulpritIds() { return this.culprits; }
  
  @Exported
  @NonNull
  public Set<User> getCulprits() { return super.getCulprits(); }
  
  public boolean shouldCalculateCulprits() { return (getCulpritIds() == null); }
  
  @NonNull
  public Set<User> calculateCulprits() {
    Set<User> c = super.calculateCulprits();
    AbstractBuild<P, R> p = (AbstractBuild)getPreviousCompletedBuild();
    if (upstreamCulprits)
      if (p != null && p.getPreviousNotFailedBuild() != null) {
        Map<AbstractProject, DependencyChange> depmap = p.getDependencyChanges((AbstractBuild)p.getPreviousSuccessfulBuild());
        for (DependencyChange dep : depmap.values()) {
          for (AbstractBuild<?, ?> b : dep.getBuilds()) {
            for (ChangeLogSet.Entry entry : b.getChangeSet())
              c.add(entry.getAuthor()); 
          } 
        } 
      }  
    return c;
  }
  
  public String getHudsonVersion() { return this.hudsonVersion; }
  
  @Exported
  @NonNull
  public ChangeLogSet<? extends ChangeLogSet.Entry> getChangeSet() {
    synchronized (this.changeSetLock) {
      if (this.scm == null)
        this.scm = NullChangeLogParser.INSTANCE; 
    } 
    ChangeLogSet<? extends ChangeLogSet.Entry> cs = null;
    if (this.changeSet != null)
      cs = (ChangeLogSet)this.changeSet.get(); 
    if (cs == null)
      cs = calcChangeSet(); 
    if (cs == null)
      cs = ChangeLogSet.createEmpty(this); 
    this.changeSet = new WeakReference(cs);
    return cs;
  }
  
  @NonNull
  public List<ChangeLogSet<? extends ChangeLogSet.Entry>> getChangeSets() {
    ChangeLogSet<? extends ChangeLogSet.Entry> cs = getChangeSet();
    return cs.isEmptySet() ? Collections.emptyList() : List.of(cs);
  }
  
  public boolean hasChangeSetComputed() {
    File changelogFile = new File(getRootDir(), "changelog.xml");
    return changelogFile.exists();
  }
  
  private ChangeLogSet<? extends ChangeLogSet.Entry> calcChangeSet() {
    File changelogFile = new File(getRootDir(), "changelog.xml");
    if (!changelogFile.exists())
      return ChangeLogSet.createEmpty(this); 
    try {
      return this.scm.parse(this, changelogFile);
    } catch (IOException|org.xml.sax.SAXException e) {
      LOGGER.log(Level.WARNING, "Failed to parse " + changelogFile, e);
      return ChangeLogSet.createEmpty(this);
    } 
  }
  
  @NonNull
  public EnvVars getEnvironment(@NonNull TaskListener log) throws IOException, InterruptedException {
    EnvVars env = super.getEnvironment(log);
    FilePath ws = getWorkspace();
    if (ws != null) {
      env.put("WORKSPACE", ws.getRemote());
      FilePath tempDir = WorkspaceList.tempDir(ws);
      if (tempDir != null)
        env.put("WORKSPACE_TMP", tempDir.getRemote()); 
    } 
    ((AbstractProject)this.project).getScm().buildEnvVars(this, env);
    if (this.buildEnvironments != null)
      for (Environment e : this.buildEnvironments)
        e.buildEnvVars(env);  
    for (EnvironmentContributingAction a : getActions(EnvironmentContributingAction.class))
      a.buildEnvVars(this, env); 
    EnvVars.resolve(env);
    return env;
  }
  
  public EnvironmentList getEnvironments() {
    Executor e = Executor.currentExecutor();
    if (e != null && e.getCurrentExecutable() == this) {
      if (this.buildEnvironments == null)
        this.buildEnvironments = new ArrayList(); 
      return new EnvironmentList(this.buildEnvironments);
    } 
    return new EnvironmentList((this.buildEnvironments == null) ? Collections.emptyList() : List.copyOf(this.buildEnvironments));
  }
  
  public Calendar due() { return getTimestamp(); }
  
  public void addAction(@NonNull Action a) { super.addAction(a); }
  
  public List<Action> getPersistentActions() { return getActions(); }
  
  public Set<String> getSensitiveBuildVariables() {
    Set<String> s = new HashSet<String>();
    ParametersAction parameters = (ParametersAction)getAction(ParametersAction.class);
    if (parameters != null)
      for (ParameterValue p : parameters) {
        if (p.isSensitive())
          s.add(p.getName()); 
      }  
    if (this.project instanceof BuildableItemWithBuildWrappers)
      for (BuildWrapper bw : ((BuildableItemWithBuildWrappers)this.project).getBuildWrappersList())
        bw.makeSensitiveBuildVariables(this, s);  
    return s;
  }
  
  public Map<String, String> getBuildVariables() {
    Map<String, String> r = new HashMap<String, String>();
    ParametersAction parameters = (ParametersAction)getAction(ParametersAction.class);
    if (parameters != null)
      for (ParameterValue p : parameters) {
        String v = (String)p.createVariableResolver(this).resolve(p.getName());
        if (v != null)
          r.put(p.getName(), v); 
      }  
    if (this.project instanceof BuildableItemWithBuildWrappers)
      for (BuildWrapper bw : ((BuildableItemWithBuildWrappers)this.project).getBuildWrappersList())
        bw.makeBuildVariables(this, r);  
    for (BuildVariableContributor bvc : BuildVariableContributor.all())
      bvc.buildVariablesFor(this, r); 
    return r;
  }
  
  public final VariableResolver<String> getBuildVariableResolver() { return new VariableResolver.ByMap(getBuildVariables()); }
  
  @Deprecated
  public Action getTestResultAction() {
    try {
      return getAction((Jenkins.get().getPluginManager()).uberClassLoader.loadClass("hudson.tasks.test.AbstractTestResultAction").asSubclass(Action.class));
    } catch (ClassNotFoundException x) {
      return null;
    } 
  }
  
  @Deprecated
  public Action getAggregatedTestResultAction() {
    try {
      return getAction((Jenkins.get().getPluginManager()).uberClassLoader.loadClass("hudson.tasks.test.AggregatedTestResultAction").asSubclass(Action.class));
    } catch (ClassNotFoundException x) {
      return null;
    } 
  }
  
  public String getWhyKeepLog() {
    for (AbstractProject<?, ?> p : ((AbstractProject)getParent()).getDownstreamProjects()) {
      if (!p.isKeepDependencies())
        continue; 
      AbstractBuild<?, ?> fb = p.getFirstBuild();
      if (fb == null)
        continue; 
      for (Iterator iterator = getDownstreamRelationship(p).listNumbersReverse().iterator(); iterator.hasNext(); ) {
        int i = ((Integer)iterator.next()).intValue();
        if (i < fb.getNumber())
          break; 
        AbstractBuild<?, ?> b = p.getBuildByNumber(i);
        if (b != null)
          return Messages.AbstractBuild_KeptBecause(p.hasPermission(Item.READ) ? b.toString() : "?"); 
      } 
    } 
    return super.getWhyKeepLog();
  }
  
  public Fingerprint.RangeSet getDownstreamRelationship(AbstractProject that) {
    Fingerprint.RangeSet rs = new Fingerprint.RangeSet();
    Fingerprinter.FingerprintAction f = (Fingerprinter.FingerprintAction)getAction(Fingerprinter.FingerprintAction.class);
    if (f == null)
      return rs; 
    for (Fingerprint e : f.getFingerprints().values()) {
      if (upstreamCulprits) {
        rs.add(e.getRangeSet(that));
        continue;
      } 
      Fingerprint.BuildPtr o = e.getOriginal();
      if (o != null && o.is(this))
        rs.add(e.getRangeSet(that)); 
    } 
    return rs;
  }
  
  public Iterable<AbstractBuild<?, ?>> getDownstreamBuilds(AbstractProject<?, ?> that) {
    Iterable<Integer> nums = getDownstreamRelationship(that).listNumbers();
    return new Object(this, nums, that);
  }
  
  public int getUpstreamRelationship(AbstractProject that) {
    Fingerprinter.FingerprintAction f = (Fingerprinter.FingerprintAction)getAction(Fingerprinter.FingerprintAction.class);
    if (f == null)
      return -1; 
    int n = -1;
    for (Fingerprint e : f.getFingerprints().values()) {
      if (upstreamCulprits) {
        Fingerprint.RangeSet rangeset = e.getRangeSet(that);
        if (!rangeset.isEmpty())
          n = Math.max(n, ((Integer)rangeset.listNumbersReverse().iterator().next()).intValue()); 
        continue;
      } 
      Fingerprint.BuildPtr o = e.getOriginal();
      if (o != null && o.belongsTo(that))
        n = Math.max(n, o.getNumber()); 
    } 
    return n;
  }
  
  public AbstractBuild<?, ?> getUpstreamRelationshipBuild(AbstractProject<?, ?> that) {
    int n = getUpstreamRelationship(that);
    if (n == -1)
      return null; 
    return that.getBuildByNumber(n);
  }
  
  public Map<AbstractProject, Fingerprint.RangeSet> getDownstreamBuilds() {
    Map<AbstractProject, Fingerprint.RangeSet> r = new HashMap<AbstractProject, Fingerprint.RangeSet>();
    for (AbstractProject p : ((AbstractProject)getParent()).getDownstreamProjects()) {
      if (p.isFingerprintConfigured())
        r.put(p, getDownstreamRelationship(p)); 
    } 
    return r;
  }
  
  public Map<AbstractProject, Integer> getUpstreamBuilds() { return _getUpstreamBuilds(((AbstractProject)getParent()).getUpstreamProjects()); }
  
  public Map<AbstractProject, Integer> getTransitiveUpstreamBuilds() { return _getUpstreamBuilds(((AbstractProject)getParent()).getTransitiveUpstreamProjects()); }
  
  private Map<AbstractProject, Integer> _getUpstreamBuilds(Collection<AbstractProject> projects) {
    Map<AbstractProject, Integer> r = new HashMap<AbstractProject, Integer>();
    for (AbstractProject p : projects) {
      int n = getUpstreamRelationship(p);
      if (n >= 0)
        r.put(p, Integer.valueOf(n)); 
    } 
    return r;
  }
  
  public Map<AbstractProject, DependencyChange> getDependencyChanges(AbstractBuild from) {
    if (from == null)
      return Collections.emptyMap(); 
    Fingerprinter.FingerprintAction n = (Fingerprinter.FingerprintAction)getAction(Fingerprinter.FingerprintAction.class);
    Fingerprinter.FingerprintAction o = (Fingerprinter.FingerprintAction)from.getAction(Fingerprinter.FingerprintAction.class);
    if (n == null || o == null)
      return Collections.emptyMap(); 
    Map<AbstractProject, Integer> ndep = n.getDependencies(true);
    Map<AbstractProject, Integer> odep = o.getDependencies(true);
    Map<AbstractProject, DependencyChange> r = new HashMap<AbstractProject, DependencyChange>();
    for (Map.Entry<AbstractProject, Integer> entry : odep.entrySet()) {
      AbstractProject p = (AbstractProject)entry.getKey();
      Integer oldNumber = (Integer)entry.getValue();
      Integer newNumber = (Integer)ndep.get(p);
      if (newNumber != null && oldNumber.compareTo(newNumber) < 0)
        r.put(p, new DependencyChange(p, oldNumber.intValue(), newNumber.intValue())); 
    } 
    return r;
  }
  
  @Deprecated
  @RequirePOST
  public void doStop(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { doStop().generateResponse(req, rsp, this); }
  
  @RequirePOST
  public HttpResponse doStop() throws IOException, ServletException {
    Executor e = getExecutor();
    if (e == null)
      e = getOneOffExecutor(); 
    if (e != null)
      return e.doStop(); 
    return HttpResponses.forwardToPreviousPage();
  }
  
  private static final Logger LOGGER = Logger.getLogger(AbstractBuild.class.getName());
  
  public abstract void run();
}
