package jenkins.security.s2m;

import hudson.Extension;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
@Extension
public class AdminWhitelistRule {
  public boolean getMasterKillSwitch() { return false; }
  
  public void setMasterKillSwitch(boolean state) {
    if (state) {
      LOGGER.log(Level.WARNING, "Setting AdminWhitelistRule no longer has any effect. See https://www.jenkins.io/redirect/AdminWhitelistRule to learn more.", new Exception());
    } else {
      LOGGER.log(Level.INFO, "Setting AdminWhitelistRule no longer has any effect. See https://www.jenkins.io/redirect/AdminWhitelistRule to learn more.", new Exception());
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(AdminWhitelistRule.class.getName());
}
