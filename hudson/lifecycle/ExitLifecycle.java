package hudson.lifecycle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class ExitLifecycle extends Lifecycle {
  private static final Logger LOGGER = Logger.getLogger(ExitLifecycle.class.getName());
  
  private static final String EXIT_CODE_ON_RESTART = "exitCodeOnRestart";
  
  private static final String DEFAULT_EXIT_CODE = "5";
  
  private Integer exitOnRestart = Integer.valueOf(Integer.parseInt(SystemProperties.getString(Jenkins.class.getName() + ".exitCodeOnRestart", "5")));
  
  @SuppressFBWarnings(value = {"DM_EXIT"}, justification = "Exit is really intended.")
  public void restart() {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    try {
      if (jenkins != null)
        jenkins.cleanUp(); 
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to clean up. Restart will continue.", e);
    } 
    System.exit(this.exitOnRestart.intValue());
  }
}
