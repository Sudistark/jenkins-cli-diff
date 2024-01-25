package jenkins.diagnostics;

import hudson.Extension;
import hudson.Main;
import hudson.model.AdministrativeMonitor;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"executorsOnBuiltInNodeWithAgents", "controllerExecutorsWithAgents"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ControllerExecutorsAgents extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.ControllerExecutorsAgents_DisplayName(); }
  
  public boolean isSecurity() { return true; }
  
  public boolean isActivated() {
    return (!Main.isDevelopmentMode && Jenkins.get().getNumExecutors() > 0 && (
      !(Jenkins.get()).clouds.isEmpty() || !Jenkins.get().getNodes().isEmpty()));
  }
  
  @RequirePOST
  public void doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (req.hasParameter("no")) {
      disable(true);
      rsp.sendRedirect(req.getContextPath() + "/manage");
    } else if (req.hasParameter("yes")) {
      rsp.sendRedirect(req.getContextPath() + "/computer/(built-in)/configure");
    } 
  }
}
