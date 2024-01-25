package hudson.slaves;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.model.Node;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.jcip.annotations.GuardedBy;

public class CloudSlaveRetentionStrategy<T extends Computer> extends RetentionStrategy<T> {
  @GuardedBy("hudson.model.Queue.lock")
  public long check(T c) {
    if (!c.isConnecting() && c.isAcceptingTasks() && 
      isIdleForTooLong(c))
      try {
        Node n = c.getNode();
        if (n != null)
          kill(n); 
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to remove " + c.getDisplayName(), e);
      }  
    return checkCycle();
  }
  
  protected void kill(Node n) throws IOException { Jenkins.get().removeNode(n); }
  
  protected long checkCycle() { return getIdleMaxTime() / 10L; }
  
  protected boolean isIdleForTooLong(T c) { return (System.currentTimeMillis() - c.getIdleStartMilliseconds() > getIdleMaxTime()); }
  
  protected long getIdleMaxTime() { return TIMEOUT; }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static long TIMEOUT = SystemProperties.getLong(CloudSlaveRetentionStrategy.class.getName() + ".timeout", Long.valueOf(TimeUnit.MINUTES.toMillis(10L))).longValue();
  
  private static final Logger LOGGER = Logger.getLogger(CloudSlaveRetentionStrategy.class.getName());
}
