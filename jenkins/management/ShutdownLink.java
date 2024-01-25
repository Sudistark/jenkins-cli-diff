package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

@Extension(ordinal = -2.147483648E9D)
@Symbol({"prepareQuietDown"})
public class ShutdownLink extends ManagementLink {
  private static final Logger LOGGER = Logger.getLogger(ShutdownLink.class.getName());
  
  public String getIconFileName() { return "symbol-power"; }
  
  public String getDisplayName() { return Jenkins.get().isQuietingDown() ? Messages.ShutdownLink_DisplayName_update() : Messages.ShutdownLink_DisplayName_prepare(); }
  
  public String getDescription() { return Jenkins.get().isQuietingDown() ? Messages.ShutdownLink_ShuttingDownInProgressDescription() : Messages.ShutdownLink_Description(); }
  
  public String getUrlName() { return "prepareShutdown"; }
  
  @POST
  public void doPrepare(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
    Jenkins.get().checkPermission(Jenkins.MANAGE);
    JSONObject submittedForm = req.getSubmittedForm();
    String inputReason = submittedForm.getString("shutdownReason");
    String shutdownReason = inputReason.isEmpty() ? null : inputReason;
    LOGGER.log(Level.FINE, "Shutdown requested by user {0}", Jenkins.getAuthentication().getName());
    Jenkins.get().doQuietDown(false, 0, shutdownReason).generateResponse(req, rsp, null);
  }
  
  @POST
  public void doCancel(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
    Jenkins.get().checkPermission(Jenkins.MANAGE);
    LOGGER.log(Level.FINE, "Shutdown cancel requested by user {0}", Jenkins.getAuthentication().getName());
    Jenkins.get().doCancelQuietDown().generateResponse(req, rsp, null);
  }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.MANAGE; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.TOOLS; }
}
