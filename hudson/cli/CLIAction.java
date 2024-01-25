package hudson.cli;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.FullDuplexHttpService;
import jenkins.util.SystemProperties;
import jenkins.websocket.WebSockets;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.security.core.Authentication;

@Extension
@Symbol({"cli"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CLIAction implements UnprotectedRootAction, StaplerProxy {
  private static final Logger LOGGER = Logger.getLogger(CLIAction.class.getName());
  
  static Boolean ALLOW_WEBSOCKET = SystemProperties.optBoolean(CLIAction.class.getName() + ".ALLOW_WEBSOCKET");
  
  private final Map<UUID, FullDuplexHttpService> duplexServices = new HashMap();
  
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return "Jenkins CLI"; }
  
  public String getUrlName() { return "cli"; }
  
  public void doCommand(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.READ);
    String commandName = req.getRestOfPath().substring(1);
    CLICommand command = CLICommand.clone(commandName);
    if (command == null) {
      rsp.sendError(404, "No such command");
      return;
    } 
    req.setAttribute("command", command);
    req.getView(this, "command.jelly").forward(req, rsp);
  }
  
  public boolean isWebSocketSupported() { return WebSockets.isSupported(); }
  
  public HttpResponse doWs(StaplerRequest req) {
    if (!WebSockets.isSupported())
      return HttpResponses.notFound(); 
    if (ALLOW_WEBSOCKET == null) {
      String actualOrigin = req.getHeader("Origin");
      String expectedOrigin = StringUtils.removeEnd(StringUtils.removeEnd(Jenkins.get().getRootUrlFromRequest(), "/"), req.getContextPath());
      if (actualOrigin == null || !actualOrigin.equals(expectedOrigin)) {
        LOGGER.log(Level.FINE, () -> "Rejecting origin: " + actualOrigin + "; expected was from request: " + expectedOrigin);
        return HttpResponses.forbidden();
      } 
    } else if (!ALLOW_WEBSOCKET.booleanValue()) {
      return HttpResponses.forbidden();
    } 
    Authentication authentication = Jenkins.getAuthentication2();
    return WebSockets.upgrade(new Object(this, authentication));
  }
  
  public Object getTarget() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req.getRestOfPath().length() == 0 && "POST".equals(req.getMethod())) {
      if ("false".equals(req.getParameter("remoting")))
        throw new PlainCliEndpointResponse(this); 
      throw HttpResponses.forbidden();
    } 
    return this;
  }
}
