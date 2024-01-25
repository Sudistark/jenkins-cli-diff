package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.PersistentDescriptor;
import hudson.util.DescribableList;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol({"artifactManager"})
public class ArtifactManagerConfiguration extends GlobalConfiguration implements PersistentDescriptor {
  @NonNull
  public static ArtifactManagerConfiguration get() { return (ArtifactManagerConfiguration)GlobalConfiguration.all().getInstance(ArtifactManagerConfiguration.class); }
  
  private final DescribableList<ArtifactManagerFactory, ArtifactManagerFactoryDescriptor> artifactManagerFactories = new DescribableList(this);
  
  private Object readResolve() {
    this.artifactManagerFactories.setOwner(this);
    return this;
  }
  
  public DescribableList<ArtifactManagerFactory, ArtifactManagerFactoryDescriptor> getArtifactManagerFactories() { return this.artifactManagerFactories; }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      this.artifactManagerFactories.rebuildHetero(req, json, ArtifactManagerFactoryDescriptor.all(), "artifactManagerFactories");
      return true;
    } catch (IOException x) {
      throw new Descriptor.FormException(x, "artifactManagerFactories");
    } 
  }
}
