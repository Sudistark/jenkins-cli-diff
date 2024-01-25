package jenkins.diagnostics;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.util.UrlHelper;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Symbol({"rootUrlNotSet"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class RootUrlNotSetMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.RootUrlNotSetMonitor_DisplayName(); }
  
  public boolean isActivated() {
    JenkinsLocationConfiguration loc = JenkinsLocationConfiguration.get();
    return (loc.getUrl() == null || !UrlHelper.isValidRootUrl(loc.getUrl()));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isUrlNull() {
    JenkinsLocationConfiguration loc = JenkinsLocationConfiguration.get();
    return (loc.getUrl() == null);
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
}
