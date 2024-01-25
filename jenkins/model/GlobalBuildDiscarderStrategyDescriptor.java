package jenkins.model;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;

public abstract class GlobalBuildDiscarderStrategyDescriptor extends Descriptor<GlobalBuildDiscarderStrategy> {
  public static DescriptorExtensionList<GlobalBuildDiscarderStrategy, GlobalBuildDiscarderStrategyDescriptor> all() { return Jenkins.get().getDescriptorList(GlobalBuildDiscarderStrategy.class); }
}
