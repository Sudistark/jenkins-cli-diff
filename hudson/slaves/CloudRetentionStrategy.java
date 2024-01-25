package hudson.slaves;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import net.jcip.annotations.GuardedBy;

public class CloudRetentionStrategy extends RetentionStrategy<AbstractCloudComputer> {
  private int idleMinutes;
  
  public CloudRetentionStrategy(int idleMinutes) { this.idleMinutes = idleMinutes; }
  
  @GuardedBy("hudson.model.Queue.lock")
  public long check(AbstractCloudComputer c) {
    AbstractCloudSlave computerNode = c.getNode();
    if (c.isIdle() && !disabled && computerNode != null) {
      long idleMilliseconds = System.currentTimeMillis() - c.getIdleStartMilliseconds();
      if (idleMilliseconds > TimeUnit.MINUTES.toMillis(this.idleMinutes)) {
        LOGGER.log(Level.INFO, "Disconnecting {0}", c.getName());
        try {
          computerNode.terminate();
        } catch (InterruptedException|java.io.IOException e) {
          LOGGER.log(Level.WARNING, "Failed to terminate " + c.getName(), e);
        } 
      } 
    } 
    return 1L;
  }
  
  public void start(AbstractCloudComputer c) { c.connect(false); }
  
  private static final Logger LOGGER = Logger.getLogger(CloudRetentionStrategy.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean disabled = SystemProperties.getBoolean(CloudRetentionStrategy.class.getName() + ".disabled");
}
