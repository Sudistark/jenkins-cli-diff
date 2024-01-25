package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.Permission;
import java.io.IOException;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 395.0D)
@Symbol({"scmRetryCount"})
public class GlobalSCMRetryCountConfiguration extends GlobalConfiguration {
  public int getScmCheckoutRetryCount() { return Jenkins.get().getScmCheckoutRetryCount(); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      Jenkins.get().setScmCheckoutRetryCount(json.getInt("scmCheckoutRetryCount"));
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "quietPeriod");
    } catch (JSONException e) {
      throw new Descriptor.FormException(e.getMessage(), "quietPeriod");
    } 
  }
  
  @NonNull
  public Permission getRequiredGlobalConfigPagePermission() { return Jenkins.MANAGE; }
}
