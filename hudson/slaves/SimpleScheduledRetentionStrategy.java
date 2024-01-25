package hudson.slaves;

import hudson.model.Computer;
import hudson.model.Queue;
import hudson.scheduler.CronTabList;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jcip.annotations.GuardedBy;
import org.kohsuke.stapler.DataBoundConstructor;

public class SimpleScheduledRetentionStrategy extends RetentionStrategy<SlaveComputer> {
  private static final Logger LOGGER = Logger.getLogger(SimpleScheduledRetentionStrategy.class.getName());
  
  private final String startTimeSpec;
  
  private CronTabList tabs;
  
  private Calendar lastChecked;
  
  private long nextStop;
  
  private long nextStart;
  
  private long lastStop;
  
  private long lastStart;
  
  private final int upTimeMins;
  
  private final boolean keepUpWhenActive;
  
  @DataBoundConstructor
  public SimpleScheduledRetentionStrategy(String startTimeSpec, int upTimeMins, boolean keepUpWhenActive) {
    this.nextStop = Float.MIN_VALUE;
    this.nextStart = Float.MIN_VALUE;
    this.lastStop = Float.MAX_VALUE;
    this.lastStart = Float.MAX_VALUE;
    this.startTimeSpec = startTimeSpec;
    this.keepUpWhenActive = keepUpWhenActive;
    this.tabs = CronTabList.create(startTimeSpec);
    this.lastChecked = new GregorianCalendar();
    this.upTimeMins = Math.max(1, upTimeMins);
    this.lastChecked.add(12, -1);
  }
  
  public int getUpTimeMins() { return this.upTimeMins; }
  
  public boolean isKeepUpWhenActive() { return this.keepUpWhenActive; }
  
  public String getStartTimeSpec() { return this.startTimeSpec; }
  
  private void updateStartStopWindow() {
    if (this.lastStart == Float.MAX_VALUE && this.lastStop == Float.MAX_VALUE) {
      Calendar time = new GregorianCalendar();
      time.add(12, -this.upTimeMins);
      time.add(12, -this.upTimeMins);
      time.add(12, -this.upTimeMins);
      this.lastStart = time.getTimeInMillis();
      time.add(12, this.upTimeMins);
      this.lastStop = time.getTimeInMillis();
      time = new GregorianCalendar();
      time.add(12, -this.upTimeMins);
      time.add(12, -1);
      while (System.currentTimeMillis() + 1000L > time.getTimeInMillis()) {
        if (this.tabs.check(time)) {
          this.lastStart = time.getTimeInMillis();
          time.add(12, this.upTimeMins);
          this.lastStop = time.getTimeInMillis();
          break;
        } 
        time.add(12, 1);
      } 
      this.nextStart = this.lastStart;
      this.nextStop = this.lastStop;
    } 
    if (this.nextStop < System.currentTimeMillis()) {
      this.lastStart = this.nextStart;
      this.lastStop = this.nextStop;
      Calendar time = new GregorianCalendar();
      time.add(12, Math.min(15, this.upTimeMins));
      long stopLooking = time.getTimeInMillis();
      time.setTimeInMillis(this.nextStop);
      while (stopLooking > time.getTimeInMillis()) {
        if (this.tabs.check(time)) {
          this.nextStart = time.getTimeInMillis();
          time.add(12, this.upTimeMins);
          this.nextStop = time.getTimeInMillis();
          break;
        } 
        time.add(12, 1);
      } 
    } 
  }
  
  protected Object readResolve() throws ObjectStreamException {
    try {
      this.tabs = CronTabList.create(this.startTimeSpec);
      this.lastChecked = new GregorianCalendar();
      this.lastChecked.add(12, -1);
      this.nextStop = Float.MIN_VALUE;
      this.nextStart = Float.MIN_VALUE;
      this.lastStop = Float.MAX_VALUE;
      this.lastStart = Float.MAX_VALUE;
    } catch (IllegalArgumentException e) {
      InvalidObjectException x = new InvalidObjectException(e.getMessage());
      x.initCause(e);
      throw x;
    } 
    return this;
  }
  
  public boolean isManualLaunchAllowed(SlaveComputer c) { return isOnlineScheduled(); }
  
  @GuardedBy("hudson.model.Queue.lock")
  public long check(SlaveComputer c) {
    boolean shouldBeOnline = isOnlineScheduled();
    LOGGER.log(Level.FINE, "Checking computer {0} against schedule. online = {1}, shouldBeOnline = {2}", new Object[] { c
          .getName(), Boolean.valueOf(c.isOnline()), Boolean.valueOf(shouldBeOnline) });
    if (shouldBeOnline && c.isOffline()) {
      LOGGER.log(Level.INFO, "Trying to launch computer {0} as schedule says it should be on-line at this point in time", new Object[] { c
            .getName() });
      if (c.isLaunchSupported())
        Computer.threadPoolForRemoting.submit(new Object(this, c)); 
    } else if (!shouldBeOnline && c.isOnline()) {
      if (this.keepUpWhenActive) {
        if (!c.isIdle() && c.isAcceptingTasks()) {
          c.setAcceptingTasks(false);
          LOGGER.log(Level.INFO, "Disabling new jobs for computer {0} as it has finished its scheduled uptime", new Object[] { c
                
                .getName() });
          return 1L;
        } 
        if (c.isIdle() && c.isAcceptingTasks()) {
          Queue.withLock(new Object(this, c));
        } else if (c.isIdle() && !c.isAcceptingTasks()) {
          Queue.withLock(new Object(this, c));
        } 
      } else {
        LOGGER.log(Level.INFO, "Disconnecting computer {0} as it has finished its scheduled uptime", new Object[] { c
              .getName() });
        c.disconnect(OfflineCause.create(Messages._SimpleScheduledRetentionStrategy_FinishedUpTime()));
      } 
    } 
    return 1L;
  }
  
  private boolean isOnlineScheduled() {
    updateStartStopWindow();
    long now = System.currentTimeMillis();
    return ((this.lastStart < now && this.lastStop > now) || (this.nextStart < now && this.nextStop > now));
  }
}
