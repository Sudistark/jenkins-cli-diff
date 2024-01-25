package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.Permission;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 400.0D)
@Symbol({"quietPeriod"})
public class GlobalQuietPeriodConfiguration extends GlobalConfiguration {
  public int getQuietPeriod() { return Jenkins.get().getQuietPeriod(); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    int i = 0;
    try {
      i = Integer.parseInt(json.getString("quietPeriod"));
    } catch (NumberFormatException numberFormatException) {}
    try {
      Jenkins.get().setQuietPeriod(Integer.valueOf(i));
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "quietPeriod");
    } 
  }
  
  @NonNull
  public Permission getRequiredGlobalConfigPagePermission() { return Jenkins.MANAGE; }
}
