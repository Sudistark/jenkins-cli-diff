package jenkins.diagnostics;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"securityIsOff"})
public class SecurityIsOffMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.SecurityIsOffMonitor_DisplayName(); }
  
  public boolean isActivated() { return !Jenkins.get().isUseSecurity(); }
  
  public boolean isSecurity() { return true; }
  
  @RequirePOST
  public void doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (req.hasParameter("no")) {
      disable(true);
      rsp.sendRedirect(req.getContextPath() + "/manage");
    } else {
      rsp.sendRedirect(req.getContextPath() + "/configureSecurity");
    } 
  }
}
