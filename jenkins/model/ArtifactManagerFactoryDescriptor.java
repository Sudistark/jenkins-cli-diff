package jenkins.model;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;

public abstract class ArtifactManagerFactoryDescriptor extends Descriptor<ArtifactManagerFactory> {
  public static DescriptorExtensionList<ArtifactManagerFactory, ArtifactManagerFactoryDescriptor> all() { return Jenkins.get().getDescriptorList(ArtifactManagerFactory.class); }
}
