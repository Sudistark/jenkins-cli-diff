package hudson.tasks;

import hudson.DescriptorExtensionList;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Project;
import jenkins.model.Jenkins;

public abstract class Publisher extends BuildStepCompatibilityLayer implements Describable<Publisher> {
  @Deprecated
  public boolean prebuild(Build build, BuildListener listener) { return true; }
  
  @Deprecated
  public Action getProjectAction(Project project) { return null; }
  
  public boolean needsToRunAfterFinalized() { return false; }
  
  public Descriptor<Publisher> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<Publisher, Descriptor<Publisher>> all() { return Jenkins.get().getDescriptorList(Publisher.class); }
}
