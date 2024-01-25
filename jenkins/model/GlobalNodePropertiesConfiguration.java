package jenkins.model;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.NodeProperty;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 110.0D)
@Symbol({"nodeProperties"})
public class GlobalNodePropertiesConfiguration extends GlobalConfiguration {
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      Jenkins j = Jenkins.get();
      JSONObject np = json.getJSONObject("globalNodeProperties");
      if (!np.isNullObject())
        j.getGlobalNodeProperties().rebuild(req, np, NodeProperty.for_(j)); 
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "globalNodeProperties");
    } 
  }
}
