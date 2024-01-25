package hudson.tasks;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;

public abstract class BuildWrapperDescriptor extends Descriptor<BuildWrapper> {
  protected BuildWrapperDescriptor(Class<? extends BuildWrapper> clazz) { super(clazz); }
  
  protected BuildWrapperDescriptor() {}
  
  public abstract boolean isApplicable(AbstractProject<?, ?> paramAbstractProject);
}
