package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepMonitor;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class JobProperty<J extends Job<?, ?>> extends Object implements ReconfigurableDescribable<JobProperty<?>>, BuildStep, ExtensionPoint {
  protected J owner;
  
  protected void setOwner(J owner) { this.owner = owner; }
  
  public JobPropertyDescriptor getDescriptor() { return (JobPropertyDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @Deprecated
  public Action getJobAction(J job) { return null; }
  
  @NonNull
  public Collection<? extends Action> getJobActions(J job) {
    Action a = getJobAction(job);
    if (a == null)
      return Collections.emptyList(); 
    return List.of(a);
  }
  
  public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) { return true; }
  
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException { return true; }
  
  public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }
  
  public final Action getProjectAction(AbstractProject<?, ?> project) { return getJobAction(project); }
  
  @NonNull
  public final Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) { return getJobActions(project); }
  
  public Collection<?> getJobOverrides() { return Collections.emptyList(); }
  
  public JobProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException { return (form == null) ? null : getDescriptor().newInstance(req, form); }
  
  public Collection<? extends SubTask> getSubTasks() { return Collections.emptyList(); }
}
