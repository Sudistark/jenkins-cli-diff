package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public abstract class GlobalConfiguration extends Descriptor<GlobalConfiguration> implements ExtensionPoint, Describable<GlobalConfiguration> {
  protected GlobalConfiguration() { super(self()); }
  
  public final Descriptor<GlobalConfiguration> getDescriptor() { return this; }
  
  public String getGlobalConfigPage() { return getConfigPage(); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    req.bindJSON(this, json);
    return true;
  }
  
  @NonNull
  public static ExtensionList<GlobalConfiguration> all() { return Jenkins.get().getDescriptorList(GlobalConfiguration.class); }
}
