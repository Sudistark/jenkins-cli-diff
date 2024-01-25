package hudson.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.console.ModelHyperlinkNote;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.DependencyGraph;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.Result;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.DependencyDeclarer;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuildTrigger extends Recorder implements DependencyDeclarer {
  private String childProjects;
  
  private final Result threshold;
  
  public BuildTrigger(String childProjects, boolean evenIfUnstable) { this(childProjects, evenIfUnstable ? Result.UNSTABLE : Result.SUCCESS); }
  
  @DataBoundConstructor
  public BuildTrigger(String childProjects, String threshold) { this(childProjects, Result.fromString(StringUtils.defaultString(threshold, Result.SUCCESS.toString()))); }
  
  public BuildTrigger(String childProjects, Result threshold) {
    if (childProjects == null)
      throw new IllegalArgumentException(); 
    this.childProjects = childProjects;
    this.threshold = threshold;
  }
  
  public BuildTrigger(List<AbstractProject> childProjects, Result threshold) { this(childProjects, threshold); }
  
  public BuildTrigger(Collection<? extends AbstractProject> childProjects, Result threshold) { this(Items.toNameList(childProjects), threshold); }
  
  public String getChildProjectsValue() { return this.childProjects; }
  
  public Result getThreshold() {
    if (this.threshold == null)
      return Result.SUCCESS; 
    return this.threshold;
  }
  
  @Deprecated
  public List<AbstractProject> getChildProjects() { return getChildProjects(Jenkins.get()); }
  
  @Deprecated
  public List<AbstractProject> getChildProjects(AbstractProject owner) { return getChildProjects((owner == null) ? null : owner.getParent()); }
  
  @Deprecated
  public List<AbstractProject> getChildProjects(ItemGroup base) { return Items.fromNameList(base, this.childProjects, AbstractProject.class); }
  
  @NonNull
  public List<Job<?, ?>> getChildJobs(@NonNull AbstractProject<?, ?> owner) { return Items.fromNameList(owner.getParent(), this.childProjects, Job.class); }
  
  public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }
  
  @Deprecated
  public boolean hasSame(AbstractProject owner, Collection<? extends AbstractProject> projects) {
    List<AbstractProject> children = getChildProjects(owner);
    return (children.size() == projects.size() && children.containsAll(projects));
  }
  
  @Deprecated
  public boolean hasSame(Collection<? extends AbstractProject> projects) { return hasSame(null, projects); }
  
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    List<Job<?, ?>> jobs = new ArrayList<Job<?, ?>>();
    for (Job<?, ?> job : getChildJobs(build.getProject())) {
      if (job instanceof AbstractProject)
        continue; 
      jobs.add(job);
    } 
    if (!jobs.isEmpty() && build.getResult().isBetterOrEqualTo(this.threshold)) {
      PrintStream logger = listener.getLogger();
      for (Job<?, ?> downstream : jobs) {
        if (Jenkins.get().getItemByFullName(downstream.getFullName()) != downstream) {
          LOGGER.log(Level.WARNING, "Running as {0} cannot even see {1} for trigger from {2}", new Object[] { Jenkins.getAuthentication2().getName(), downstream, build.getParent() });
          continue;
        } 
        if (!downstream.hasPermission(Item.BUILD)) {
          listener.getLogger().println(Messages.BuildTrigger_you_have_no_permission_to_build_(ModelHyperlinkNote.encodeTo(downstream)));
          continue;
        } 
        if (!(downstream instanceof ParameterizedJobMixIn.ParameterizedJob)) {
          logger.println(Messages.BuildTrigger_NotBuildable(ModelHyperlinkNote.encodeTo(downstream)));
          continue;
        } 
        ParameterizedJobMixIn.ParameterizedJob<?, ?> pj = (ParameterizedJobMixIn.ParameterizedJob)downstream;
        if (pj.isDisabled()) {
          logger.println(Messages.BuildTrigger_Disabled(ModelHyperlinkNote.encodeTo(downstream)));
          continue;
        } 
        if (!downstream.isBuildable()) {
          logger.println(Messages.BuildTrigger_NotBuildable(ModelHyperlinkNote.encodeTo(downstream)));
          continue;
        } 
        boolean scheduled = pj.scheduleBuild(pj.getQuietPeriod(), new Cause.UpstreamCause(build));
        if (Jenkins.get().getItemByFullName(downstream.getFullName()) == downstream) {
          String name = ModelHyperlinkNote.encodeTo(downstream);
          if (scheduled) {
            logger.println(Messages.BuildTrigger_Triggering(name));
            continue;
          } 
          logger.println(Messages.BuildTrigger_InQueue(name));
        } 
      } 
    } 
    return true;
  }
  
  @Deprecated
  public static boolean execute(AbstractBuild build, BuildListener listener, BuildTrigger trigger) { return execute(build, listener); }
  
  public static boolean execute(AbstractBuild build, BuildListener listener) {
    DependencyGraph graphTemp;
    PrintStream logger = listener.getLogger();
    try {
      Future<DependencyGraph> futureDependencyGraph = Jenkins.get().getFutureDependencyGraph();
      if (futureDependencyGraph != null) {
        graphTemp = (DependencyGraph)futureDependencyGraph.get();
      } else {
        graphTemp = Jenkins.get().getDependencyGraph();
      } 
    } catch (IllegalStateException|InterruptedException|java.util.concurrent.ExecutionException e) {
      graphTemp = Jenkins.get().getDependencyGraph();
    } 
    DependencyGraph graph = graphTemp;
    List<DependencyGraph.Dependency> downstreamProjects = new ArrayList<DependencyGraph.Dependency>(graph.getDownstreamDependencies(build.getProject()));
    downstreamProjects.sort(new Object(graph));
    for (DependencyGraph.Dependency dep : downstreamProjects) {
      List<Action> buildActions = new ArrayList<Action>();
      if (dep.shouldTriggerBuild(build, listener, buildActions)) {
        AbstractProject p = dep.getDownstreamProject();
        if (p.isDisabled()) {
          logger.println(Messages.BuildTrigger_Disabled(ModelHyperlinkNote.encodeTo(p)));
          continue;
        } 
        boolean scheduled = p.scheduleBuild(p.getQuietPeriod(), new Cause.UpstreamCause(build), (Action[])buildActions.toArray(new Action[0]));
        if (Jenkins.get().getItemByFullName(p.getFullName()) == p) {
          String name = ModelHyperlinkNote.encodeTo(p);
          if (scheduled) {
            logger.println(Messages.BuildTrigger_Triggering(name));
            continue;
          } 
          logger.println(Messages.BuildTrigger_InQueue(name));
        } 
      } 
    } 
    return true;
  }
  
  public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
    for (AbstractProject p : getChildProjects(owner))
      graph.addDependency(new Object(this, owner, p)); 
  }
  
  public boolean needsToRunAfterFinalized() { return true; }
  
  @Deprecated
  public boolean onJobRenamed(String oldName, String newName) {
    if (!this.childProjects.contains(oldName))
      return false; 
    boolean changed = false;
    String[] projects = this.childProjects.split(",");
    for (int i = 0; i < projects.length; i++) {
      if (projects[i].trim().equals(oldName)) {
        projects[i] = newName;
        changed = true;
      } 
    } 
    if (changed)
      this.childProjects = String.join(",", projects); 
    return changed;
  }
  
  private Object readResolve() {
    if (this.childProjects == null)
      return this.childProjects = ""; 
    return this;
  }
  
  private static final Logger LOGGER = Logger.getLogger(BuildTrigger.class.getName());
}
