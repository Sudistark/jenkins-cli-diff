package hudson.diagnosis;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.PeriodicWork;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"diskUsageCheck"})
public class HudsonHomeDiskUsageChecker extends PeriodicWork {
  public long getRecurrencePeriod() { return 3600000L; }
  
  protected void doRun() {
    long free = Jenkins.get().getRootDir().getUsableSpace();
    long total = Jenkins.get().getRootDir().getTotalSpace();
    if (free <= 0L || total <= 0L) {
      LOGGER.info("JENKINS_HOME disk usage information isn't available. aborting to monitor");
      cancel();
      return;
    } 
    LOGGER.fine("Monitoring disk usage of JENKINS_HOME. total=" + total + " free=" + free);
    (HudsonHomeDiskUsageMonitor.get()).activated = (total / free > 10L && free < FREE_SPACE_THRESHOLD);
  }
  
  private static final Logger LOGGER = Logger.getLogger(HudsonHomeDiskUsageChecker.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static long FREE_SPACE_THRESHOLD = SystemProperties.getLong(HudsonHomeDiskUsageChecker.class.getName() + ".freeSpaceThreshold", 
      Long.valueOf(10737418240L)).longValue();
}
