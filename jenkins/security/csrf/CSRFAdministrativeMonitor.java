package jenkins.security.csrf;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Symbol({"csrf"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CSRFAdministrativeMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.CSRFAdministrativeMonitor_displayName(); }
  
  public boolean isActivated() { return (Jenkins.get().getCrumbIssuer() == null); }
  
  public boolean isSecurity() { return true; }
}
