package jenkins.management;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.model.Descriptor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AdministrativeMonitorsConfiguration extends GlobalConfiguration {
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    JSONArray monitors = json.optJSONArray("administrativeMonitor");
    for (AdministrativeMonitor am : AdministrativeMonitor.all()) {
      try {
        boolean disable;
        if (monitors != null) {
          disable = !monitors.contains(am.id);
        } else {
          disable = !am.id.equals(json.optString("administrativeMonitor"));
        } 
        am.disable(disable);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to process form submission for " + am.id, e);
      } 
    } 
    return true;
  }
  
  private static Logger LOGGER = Logger.getLogger(AdministrativeMonitorsConfiguration.class.getName());
}
