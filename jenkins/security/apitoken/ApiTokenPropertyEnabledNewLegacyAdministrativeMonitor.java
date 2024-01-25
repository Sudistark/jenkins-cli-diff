package jenkins.security.apitoken;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.util.HttpResponses;
import java.io.IOException;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"apiTokenNewLegacyWithoutExisting"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor_displayName(); }
  
  public boolean isActivated() { return ApiTokenPropertyConfiguration.get().isCreationOfLegacyTokenEnabled(); }
  
  public boolean isSecurity() { return true; }
  
  @RequirePOST
  public HttpResponse doAct(@QueryParameter String no) throws IOException {
    if (no == null) {
      ApiTokenPropertyConfiguration.get().setCreationOfLegacyTokenEnabled(false);
    } else {
      disable(true);
    } 
    return HttpResponses.redirectViaContextPath("manage");
  }
}
