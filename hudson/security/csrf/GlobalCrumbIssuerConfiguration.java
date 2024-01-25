package hudson.security.csrf;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Descriptor;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 195.0D)
@Symbol({"crumb"})
public class GlobalCrumbIssuerConfiguration extends GlobalConfiguration {
  @NonNull
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    Jenkins j = Jenkins.get();
    if (json.has("crumbIssuer")) {
      j.setCrumbIssuer((CrumbIssuer)req.bindJSON(CrumbIssuer.class, json.getJSONObject("crumbIssuer")));
    } else {
      j.setCrumbIssuer(createDefaultCrumbIssuer());
    } 
    return true;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public CrumbIssuer getCrumbIssuer() { return Jenkins.get().getCrumbIssuer(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static CrumbIssuer createDefaultCrumbIssuer() {
    if (DISABLE_CSRF_PROTECTION)
      return null; 
    return new DefaultCrumbIssuer(SystemProperties.getBoolean(Jenkins.class.getName() + ".crumbIssuerProxyCompatibility", false));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLE_CSRF_PROTECTION = SystemProperties.getBoolean(GlobalCrumbIssuerConfiguration.class.getName() + ".DISABLE_CSRF_PROTECTION");
}
