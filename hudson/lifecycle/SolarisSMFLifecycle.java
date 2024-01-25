package hudson.lifecycle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public class SolarisSMFLifecycle extends Lifecycle {
  @SuppressFBWarnings(value = {"DM_EXIT"}, justification = "Exit is really intended.")
  public void restart() {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    try {
      if (jenkins != null)
        jenkins.cleanUp(); 
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to clean up. Restart will continue.", e);
    } 
    System.exit(0);
  }
  
  private static final Logger LOGGER = Logger.getLogger(SolarisSMFLifecycle.class.getName());
}
