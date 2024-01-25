package jenkins.model;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import java.io.IOException;
import javax.servlet.ServletException;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Symbol({"builtinNodeMigration"})
public class BuiltInNodeMigration extends AdministrativeMonitor {
  public boolean isActivated() { return !Jenkins.get().getRenameMigrationDone(); }
  
  @RequirePOST
  public void doAct(StaplerRequest req, StaplerResponse rsp, @QueryParameter String yes, @QueryParameter String no) throws IOException, ServletException {
    if (yes != null) {
      Jenkins.get().performRenameMigration();
    } else if (no != null) {
      disable(true);
    } 
    rsp.forwardToPreviousPage(req);
  }
  
  public String getDisplayName() { return Messages.BuiltInNodeMigration_DisplayName(); }
}
