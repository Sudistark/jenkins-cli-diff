package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.triggers.SafeTimerTask;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import jenkins.util.Timer;

@SuppressFBWarnings(value = {"PREDICTABLE_RANDOM"}, justification = "The random is just used for an initial delay.")
public abstract class PeriodicWork extends SafeTimerTask implements ExtensionPoint {
  @Deprecated
  protected final Logger logger = Logger.getLogger(getClass().getName());
  
  protected static final long MIN = 60000L;
  
  protected static final long HOUR = 3600000L;
  
  protected static final long DAY = 86400000L;
  
  public abstract long getRecurrencePeriod();
  
  public long getInitialDelay() {
    long l = RANDOM.nextLong();
    if (l == Float.MIN_VALUE)
      l++; 
    return Math.abs(l) % getRecurrencePeriod();
  }
  
  public static ExtensionList<PeriodicWork> all() { return ExtensionList.lookup(PeriodicWork.class); }
  
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void init() {
    extensionList = all();
    extensionList.addListener(new PeriodicWorkExtensionListListener(extensionList));
    for (PeriodicWork p : extensionList)
      schedulePeriodicWork(p); 
  }
  
  private static void schedulePeriodicWork(PeriodicWork p) { Timer.get().scheduleAtFixedRate(p, p.getInitialDelay(), p.getRecurrencePeriod(), TimeUnit.MILLISECONDS); }
  
  private static final Random RANDOM = new Random();
}
