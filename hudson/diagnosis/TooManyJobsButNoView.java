package hudson.diagnosis;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"tooManyJobsButNoView"})
public class TooManyJobsButNoView extends AdministrativeMonitor {
  public static final int THRESHOLD = 16;
  
  public String getDisplayName() { return Messages.TooManyJobsButNoView_DisplayName(); }
  
  public boolean isActivated() {
    Jenkins j = Jenkins.get();
    if (j.hasPermission(Jenkins.ADMINISTER))
      return (j.getViews().size() == 1 && j.getItemMap().size() > 16); 
    return (j.getViews().size() == 1 && j.getItems().size() > 16);
  }
  
  @RequirePOST
  public void doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (req.hasParameter("no")) {
      disable(true);
      rsp.sendRedirect(req.getContextPath() + "/manage");
    } else {
      rsp.sendRedirect(req.getContextPath() + "/newView");
    } 
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
}
