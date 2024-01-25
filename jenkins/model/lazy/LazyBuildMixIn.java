package jenkins.model.lazy;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.RunMap;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.RunIdMigrator;

public abstract class LazyBuildMixIn<JobT extends Job<JobT, RunT> & Queue.Task & LazyBuildMixIn.LazyLoadingJob<JobT, RunT>, RunT extends Run<JobT, RunT> & LazyBuildMixIn.LazyLoadingRun<JobT, RunT>> extends Object {
  private static final Logger LOGGER = Logger.getLogger(LazyBuildMixIn.class.getName());
  
  @NonNull
  private RunMap<RunT> builds = new RunMap();
  
  @NonNull
  public final RunMap<RunT> getRunMap() { return this.builds; }
  
  public final RunMap<RunT> _getRuns() {
    assert this.builds.baseDirInitialized() : "neither onCreatedFromScratch nor onLoad called on " + asJob() + " yet";
    return this.builds;
  }
  
  public final void onCreatedFromScratch() { this.builds = createBuildRunMap(); }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    RunMap<RunT> _builds = createBuildRunMap();
    int max = _builds.maxNumberOnDisk();
    int next = asJob().getNextBuildNumber();
    if (next <= max) {
      LOGGER.log(Level.WARNING, "JENKINS-27530: improper nextBuildNumber {0} detected in {1} with highest build number {2}; adjusting", new Object[] { Integer.valueOf(next), asJob(), Integer.valueOf(max) });
      asJob().updateNextBuildNumber(max + 1);
    } 
    RunMap<RunT> currentBuilds = this.builds;
    if (parent != null) {
      Item current;
      try {
        current = parent.getItem(name);
      } catch (RuntimeException x) {
        LOGGER.log(Level.WARNING, "failed to look up " + name + " in " + parent, x);
        current = null;
      } 
      if (current != null && current.getClass() == asJob().getClass())
        currentBuilds = (((LazyLoadingJob)current).getLazyBuildMixIn()).builds; 
    } 
    if (currentBuilds != null)
      for (Iterator iterator = currentBuilds.getLoadedBuilds().values().iterator(); iterator.hasNext(); ) {
        RunT r = (RunT)(Run)iterator.next();
        if (r.isBuilding()) {
          _builds.put(Integer.valueOf(r.getNumber()), r);
          LOGGER.log(Level.FINE, "keeping reloaded {0}", r);
        } 
      }  
    this.builds = _builds;
  }
  
  private RunMap<RunT> createBuildRunMap() {
    RunMap<RunT> r = new RunMap<RunT>(asJob().getBuildDir(), new Object(this));
    RunIdMigrator runIdMigrator = (asJob()).runIdMigrator;
    assert runIdMigrator != null;
    r.runIdMigrator = runIdMigrator;
    return r;
  }
  
  public RunT loadBuild(File dir) throws IOException {
    try {
      return (RunT)(Run)getBuildClass().getConstructor(new Class[] { asJob().getClass(), File.class }).newInstance(new Object[] { asJob(), dir });
    } catch (InstantiationException|NoSuchMethodException|IllegalAccessException e) {
      throw new LinkageError(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw handleInvocationTargetException(e);
    } 
  }
  
  public final RunT newBuild() throws IOException {
    try {
      RunT lastBuild = (RunT)(Run)getBuildClass().getConstructor(new Class[] { asJob().getClass() }).newInstance(new Object[] { asJob() });
      Path rootDir = lastBuild.getRootDir().toPath();
      if (Files.isDirectory(rootDir, new java.nio.file.LinkOption[0])) {
        LOGGER.warning(() -> "JENKINS-23152: " + rootDir + " already existed; will not overwrite with " + lastBuild + " but will create a fresh build #" + asJob().getNextBuildNumber());
        return (RunT)newBuild();
      } 
      this.builds.put(lastBuild);
      lastBuild.getPreviousBuild();
      return lastBuild;
    } catch (InvocationTargetException e) {
      LOGGER.log(Level.WARNING, String.format("A new build could not be created in job %s", new Object[] { asJob().getFullName() }), e);
      throw handleInvocationTargetException(e);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("A new build could not be created in " + asJob().getFullName() + ": " + e, e);
    } catch (IllegalStateException e) {
      throw new IOException("A new build could not be created in " + asJob().getFullName() + ": " + e, e);
    } 
  }
  
  private IOException handleInvocationTargetException(InvocationTargetException e) {
    Throwable t = e.getTargetException();
    if (t instanceof Error)
      throw (Error)t; 
    if (t instanceof RuntimeException)
      throw (RuntimeException)t; 
    if (t instanceof IOException)
      return (IOException)t; 
    throw new Error(t);
  }
  
  public final void removeRun(RunT run) {
    if (!this.builds.remove(run))
      LOGGER.log(Level.WARNING, "{0} did not contain {1} to begin with", new Object[] { asJob(), run }); 
  }
  
  public final RunT getBuild(String id) { return (RunT)this.builds.getById(id); }
  
  public final RunT getBuildByNumber(int n) { return (RunT)(Run)this.builds.getByNumber(n); }
  
  public final RunT getFirstBuild() throws IOException { return (RunT)(Run)this.builds.oldestBuild(); }
  
  @CheckForNull
  public final RunT getLastBuild() throws IOException { return (RunT)(Run)this.builds.newestBuild(); }
  
  public final RunT getNearestBuild(int n) { return (RunT)(Run)this.builds.search(n, AbstractLazyLoadRunMap.Direction.ASC); }
  
  public final RunT getNearestOldBuild(int n) { return (RunT)(Run)this.builds.search(n, AbstractLazyLoadRunMap.Direction.DESC); }
  
  public List<RunT> getEstimatedDurationCandidates() {
    Collection<RunT> loadedBuilds = this.builds.getLoadedBuilds().values();
    List<RunT> candidates = new ArrayList<RunT>(3);
    for (Result threshold : List.of(Result.UNSTABLE, Result.FAILURE)) {
      for (Iterator iterator = loadedBuilds.iterator(); iterator.hasNext(); ) {
        RunT build = (RunT)(Run)iterator.next();
        if (candidates.contains(build))
          continue; 
        if (!build.isBuilding()) {
          Result result = build.getResult();
          if (result != null && result.isBetterOrEqualTo(threshold)) {
            candidates.add(build);
            if (candidates.size() == 3) {
              LOGGER.fine(() -> "Candidates: " + candidates);
              return candidates;
            } 
          } 
        } 
      } 
    } 
    LOGGER.fine(() -> "Candidates: " + candidates);
    return candidates;
  }
  
  public final HistoryWidget createHistoryWidget() { return new BuildHistoryWidget((Queue.Task)asJob(), this.builds, Job.HISTORY_ADAPTER); }
  
  protected abstract JobT asJob();
  
  protected abstract Class<RunT> getBuildClass();
}
