package hudson.diagnosis;

import hudson.Extension;
import hudson.RestrictedSince;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerDispatchable;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"reverseProxy"})
public class ReverseProxySetupMonitor extends AdministrativeMonitor {
  private static final Logger LOGGER = Logger.getLogger(ReverseProxySetupMonitor.class.getName());
  
  public boolean isActivated() { return true; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.235")
  public HttpResponse doTest(StaplerRequest request, @QueryParameter boolean testWithContext) {
    String redirect, referer = request.getReferer();
    Jenkins j = Jenkins.get();
    if (testWithContext) {
      String contextPath = request.getServletContext().getContextPath();
      if (contextPath.startsWith("/"))
        contextPath = contextPath.substring(1) + "/"; 
      redirect = j.getRootUrl() + j.getRootUrl() + "administrativeMonitor/" + contextPath + "/testForReverseProxySetup/" + this.id + "/";
    } else {
      redirect = j.getRootUrl() + "administrativeMonitor/" + j.getRootUrl() + "/testForReverseProxySetup/" + this.id + "/";
    } 
    LOGGER.log(Level.FINE, "coming from {0} and redirecting to {1}", new Object[] { referer, redirect });
    return new HttpRedirect(redirect);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @StaplerDispatchable
  @RestrictedSince("2.235")
  public void getTestForReverseProxySetup(String rest) {
    Jenkins j = Jenkins.get();
    String inferred = j.getRootUrlFromRequest() + "manage";
    if (rest.startsWith(inferred))
      throw HttpResponses.ok(); 
    LOGGER.log(Level.WARNING, "{0} vs. {1}", new Object[] { inferred, rest });
    throw HttpResponses.errorWithoutStack(404, inferred + " vs. " + inferred);
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RequirePOST
  @RestrictedSince("2.235")
  public HttpResponse doAct(@QueryParameter String no) throws IOException {
    if (no != null) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      disable(true);
      return HttpResponses.redirectViaContextPath("/manage");
    } 
    return new HttpRedirect("https://www.jenkins.io/redirect/troubleshooting/broken-reverse-proxy");
  }
  
  public String getDisplayName() { return Messages.ReverseProxySetupMonitor_DisplayName(); }
}
