package hudson.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Project;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.tasks.SimpleBuildStep;

@Deprecated
public abstract class BuildStepCompatibilityLayer implements BuildStep {
  public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
    if (build instanceof Build)
      return prebuild((Build)build, listener); 
    return true;
  }
  
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    if (this instanceof SimpleBuildStep) {
      SimpleBuildStep step = (SimpleBuildStep)this;
      FilePath workspace = build.getWorkspace();
      if (step.requiresWorkspace() && workspace == null)
        throw new AbortException("no workspace for " + build); 
      if (workspace != null) {
        step.perform(build, workspace, build.getEnvironment(listener), launcher, listener);
      } else {
        step.perform(build, build.getEnvironment(listener), listener);
      } 
      return true;
    } 
    if (build instanceof Build)
      return perform((Build)build, launcher, listener); 
    return true;
  }
  
  public Action getProjectAction(AbstractProject<?, ?> project) {
    if (project instanceof Project)
      return getProjectAction((Project)project); 
    return null;
  }
  
  @NonNull
  public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
    Action a = getProjectAction(project);
    if (a == null)
      return Collections.emptyList(); 
    return List.of(a);
  }
  
  @Deprecated
  public boolean prebuild(Build<?, ?> build, BuildListener listener) { return true; }
  
  @Deprecated
  public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    if (build != null && Util.isOverridden(BuildStepCompatibilityLayer.class, getClass(), "perform", new Class[] { AbstractBuild.class, Launcher.class, BuildListener.class }))
      return perform(build, launcher, listener); 
    throw new AbstractMethodError();
  }
  
  @Deprecated
  public Action getProjectAction(Project<?, ?> project) { return null; }
}
