package jenkins.diagnostics;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.model.AdministrativeMonitor;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
@Symbol({"completedInitialization"})
public class CompletedInitializationMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.CompletedInitializationMonitor_DisplayName(); }
  
  public boolean isActivated() {
    Jenkins instance = Jenkins.get();
    return (instance.getInitLevel() != InitMilestone.COMPLETED);
  }
}
