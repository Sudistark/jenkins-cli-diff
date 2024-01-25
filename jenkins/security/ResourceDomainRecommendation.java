package jenkins.security;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.model.DirectoryBrowserSupport;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ResourceDomainRecommendation extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.ResourceDomainConfiguration_DisplayName(); }
  
  public boolean isActivated() {
    boolean isResourceRootUrlSet = ResourceDomainConfiguration.isResourceDomainConfigured();
    boolean isOverriddenCSP = (SystemProperties.getString(DirectoryBrowserSupport.CSP_PROPERTY_NAME) != null);
    return (isOverriddenCSP && !isResourceRootUrlSet);
  }
  
  @RequirePOST
  public HttpResponse doAct(@QueryParameter String redirect, @QueryParameter String dismiss) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (dismiss != null) {
      disable(true);
      return HttpResponses.redirectViaContextPath("manage");
    } 
    if (redirect != null)
      return HttpResponses.redirectViaContextPath("configure"); 
    return HttpResponses.forwardToPreviousPage();
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  public boolean isSecurity() { return true; }
}
