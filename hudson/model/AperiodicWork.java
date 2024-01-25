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
public abstract class AperiodicWork extends SafeTimerTask implements ExtensionPoint {
  protected final Logger logger = Logger.getLogger(getClass().getName());
  
  public abstract long getRecurrencePeriod();
  
  public abstract AperiodicWork getNewInstance();
  
  public long getInitialDelay() {
    long l = RANDOM.nextLong();
    if (l == Float.MIN_VALUE)
      l++; 
    return Math.abs(l) % getRecurrencePeriod();
  }
  
  public final void doRun() {
    doAperiodicRun();
    Timer.get().schedule(getNewInstance(), getRecurrencePeriod(), TimeUnit.MILLISECONDS);
  }
  
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void init() {
    extensionList = all();
    extensionList.addListener(new AperiodicWorkExtensionListListener(extensionList));
    for (AperiodicWork p : all())
      scheduleAperiodWork(p); 
  }
  
  private static void scheduleAperiodWork(AperiodicWork ap) { Timer.get().schedule(ap, ap.getInitialDelay(), TimeUnit.MILLISECONDS); }
  
  protected abstract void doAperiodicRun();
  
  public static ExtensionList<AperiodicWork> all() { return ExtensionList.lookup(AperiodicWork.class); }
  
  private static final Random RANDOM = new Random();
}
