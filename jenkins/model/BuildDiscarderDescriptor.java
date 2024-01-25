package jenkins.model;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;

public abstract class BuildDiscarderDescriptor extends Descriptor<BuildDiscarder> {
  protected BuildDiscarderDescriptor(Class clazz) { super(clazz); }
  
  protected BuildDiscarderDescriptor() {}
  
  public static DescriptorExtensionList<BuildDiscarder, BuildDiscarderDescriptor> all() { return Jenkins.get().getDescriptorList(BuildDiscarder.class); }
}
