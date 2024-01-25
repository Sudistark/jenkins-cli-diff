package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;

public abstract class ArtifactManagerFactory extends AbstractDescribableImpl<ArtifactManagerFactory> implements ExtensionPoint {
  public ArtifactManagerFactoryDescriptor getDescriptor() { return (ArtifactManagerFactoryDescriptor)super.getDescriptor(); }
  
  @CheckForNull
  public abstract ArtifactManager managerFor(Run<?, ?> paramRun);
}
