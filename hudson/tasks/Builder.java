package hudson.tasks;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class Builder extends BuildStepCompatibilityLayer implements Describable<Builder>, ExtensionPoint {
  public boolean prebuild(Build build, BuildListener listener) { return true; }
  
  public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }
  
  public Descriptor<Builder> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<Builder, Descriptor<Builder>> all() { return Jenkins.get().getDescriptorList(Builder.class); }
}
