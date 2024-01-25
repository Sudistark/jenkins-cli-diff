package jenkins.slaves.restarter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

@Extension
public class JnlpSlaveRestarterInstaller extends ComputerListener implements Serializable {
  private static final boolean FORCE_INSTALL = Boolean.getBoolean(JnlpSlaveRestarterInstaller.class.getName() + ".forceInstall");
  
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"}, justification = "method signature does not permit plumbing through the return value")
  public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
    if (FORCE_INSTALL || c.getNode() instanceof hudson.slaves.DumbSlave)
      Computer.threadPoolForRemoting.submit(new Install(c, listener)); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(JnlpSlaveRestarterInstaller.class.getName());
  
  private static final long serialVersionUID = 1L;
}
