package jenkins.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.PersistentDescriptor;
import hudson.util.DescribableList;
import java.io.IOException;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol({"queueItemAuthenticator"})
public class QueueItemAuthenticatorConfiguration extends GlobalConfiguration implements PersistentDescriptor {
  private final DescribableList<QueueItemAuthenticator, QueueItemAuthenticatorDescriptor> authenticators = new DescribableList(this);
  
  private Object readResolve() {
    this.authenticators.setOwner(this);
    return this;
  }
  
  @NonNull
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class); }
  
  public DescribableList<QueueItemAuthenticator, QueueItemAuthenticatorDescriptor> getAuthenticators() { return this.authenticators; }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      this.authenticators.rebuildHetero(req, json, QueueItemAuthenticatorDescriptor.all(), "authenticators");
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "authenticators");
    } 
  }
  
  @NonNull
  public static QueueItemAuthenticatorConfiguration get() { return (QueueItemAuthenticatorConfiguration)GlobalConfiguration.all().getInstance(QueueItemAuthenticatorConfiguration.class); }
}
