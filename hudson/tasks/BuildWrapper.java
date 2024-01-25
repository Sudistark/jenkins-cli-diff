package hudson.tasks;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Run;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jenkins.model.Jenkins;

public abstract class BuildWrapper extends AbstractDescribableImpl<BuildWrapper> implements ExtensionPoint {
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    if (build instanceof Build && Util.isOverridden(BuildWrapper.class, getClass(), "setUp", new Class[] { Build.class, Launcher.class, BuildListener.class }))
      return setUp((Build)build, launcher, listener); 
    throw new UnsupportedOperationException("Plugin class '" + getClass().getName() + "' does not support a build of type '" + build
        .getClass().getName() + "'.");
  }
  
  @Deprecated
  public Environment setUp(Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    if (Util.isOverridden(BuildWrapper.class, getClass(), "setUp", new Class[] { AbstractBuild.class, Launcher.class, BuildListener.class }))
      return setUp(build, launcher, listener); 
    throw new AbstractMethodError("Plugin class '" + getClass().getName() + "' does not override either overload of the setUp method.");
  }
  
  public Launcher decorateLauncher(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException { return launcher; }
  
  public OutputStream decorateLogger(AbstractBuild build, OutputStream logger) throws IOException, InterruptedException, Run.RunnerAbortedException { return logger; }
  
  public void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {}
  
  @Deprecated
  public Action getProjectAction(AbstractProject job) { return null; }
  
  public Collection<? extends Action> getProjectActions(AbstractProject job) {
    Action a = getProjectAction(job);
    if (a == null)
      return Collections.emptyList(); 
    return List.of(a);
  }
  
  public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) {}
  
  public void makeSensitiveBuildVariables(AbstractBuild build, Set<String> sensitiveVariables) {}
  
  public static DescriptorExtensionList<BuildWrapper, Descriptor<BuildWrapper>> all() { return Jenkins.get().getDescriptorList(BuildWrapper.class); }
}
