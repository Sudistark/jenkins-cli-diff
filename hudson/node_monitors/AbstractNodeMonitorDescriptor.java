package hudson.node_monitors;

import hudson.Util;
import hudson.model.AdministrativeMonitor;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.Descriptor;
import hudson.slaves.OfflineCause;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import net.jcip.annotations.GuardedBy;

public abstract class AbstractNodeMonitorDescriptor<T> extends Descriptor<NodeMonitor> {
  private static long PERIOD = TimeUnit.MINUTES.toMillis(SystemProperties.getInteger(AbstractNodeMonitorDescriptor.class.getName() + ".periodMinutes", Integer.valueOf(60)).intValue());
  
  @GuardedBy("this")
  private Record inProgress;
  
  @GuardedBy("this")
  private long inProgressStarted;
  
  @Deprecated
  protected AbstractNodeMonitorDescriptor() { this(PERIOD); }
  
  @Deprecated
  protected AbstractNodeMonitorDescriptor(long interval) {
    this.record = null;
    this.inProgress = null;
    this.inProgressStarted = Float.MIN_VALUE;
    schedule(interval);
  }
  
  @Deprecated
  protected AbstractNodeMonitorDescriptor(Class<? extends NodeMonitor> clazz) { this(clazz, PERIOD); }
  
  @Deprecated
  protected AbstractNodeMonitorDescriptor(Class<? extends NodeMonitor> clazz, long interval) {
    super(clazz);
    this.record = null;
    this.inProgress = null;
    this.inProgressStarted = Float.MIN_VALUE;
    schedule(interval);
  }
  
  private void schedule(long interval) {
    Timer.get().scheduleAtFixedRate(new Object(this), interval, interval, TimeUnit.MILLISECONDS);
  }
  
  protected abstract T monitor(Computer paramComputer) throws IOException, InterruptedException;
  
  protected Map<Computer, T> monitor() throws InterruptedException {
    Map<Computer, T> data = new HashMap<Computer, T>();
    for (Computer c : Jenkins.get().getComputers()) {
      try {
        Thread.currentThread().setName("Monitoring " + c.getDisplayName() + " for " + getDisplayName());
        if (c.getChannel() == null) {
          data.put(c, null);
        } else {
          data.put(c, monitor(c));
        } 
      } catch (RuntimeException|IOException e) {
        LOGGER.log(Level.WARNING, "Failed to monitor " + c.getDisplayName() + " for " + getDisplayName(), e);
      } catch (InterruptedException e) {
        throw (InterruptedException)(new InterruptedException("Node monitoring " + c.getDisplayName() + " for " + getDisplayName() + " aborted.")).initCause(e);
      } 
    } 
    return data;
  }
  
  public T get(Computer c) throws IOException, InterruptedException {
    if (this.record == null || !this.record.data.containsKey(c)) {
      triggerUpdate();
      return null;
    } 
    return (T)this.record.data.get(c);
  }
  
  private boolean isInProgress() { return (this.inProgress != null && this.inProgress.isAlive()); }
  
  public long getTimestamp() { return (this.record == null) ? 0L : this.record.timestamp; }
  
  public String getTimestampString() {
    if (this.record == null)
      return Messages.AbstractNodeMonitorDescriptor_NoDataYet(); 
    return Util.getTimeSpanString(System.currentTimeMillis() - this.record.timestamp);
  }
  
  public boolean isIgnored() {
    NodeMonitor m = (NodeMonitor)ComputerSet.getMonitors().get(this);
    return (m == null || m.isIgnored());
  }
  
  protected boolean markOnline(Computer c) {
    if (isIgnored() || c.isOnline())
      return false; 
    c.setTemporarilyOffline(false, null);
    return true;
  }
  
  protected boolean markOffline(Computer c, OfflineCause oc) {
    if (isIgnored() || c.isTemporarilyOffline())
      return false; 
    c.setTemporarilyOffline(true, oc);
    MonitorMarkedNodeOffline no = (MonitorMarkedNodeOffline)AdministrativeMonitor.all().get(MonitorMarkedNodeOffline.class);
    if (no != null)
      no.active = true; 
    return true;
  }
  
  @Deprecated
  protected boolean markOffline(Computer c) { return markOffline(c, null); }
  
  Thread triggerUpdate() {
    if (this.inProgress != null)
      if (!this.inProgress.isAlive()) {
        LOGGER.log(Level.WARNING, "Previous {0} monitoring activity died without cleaning up after itself", 
            getDisplayName());
        this.inProgress = null;
      } else if (System.currentTimeMillis() > this.inProgressStarted + getMonitoringTimeOut() + 1000L) {
        LOGGER.log(Level.WARNING, "Previous {0} monitoring activity still in progress. Interrupting", 
            getDisplayName());
        this.inProgress.interrupt();
        this.inProgress = null;
      } else {
        return this.inProgress;
      }  
    Record t = new Record(this);
    t.start();
    this.inProgress = t;
    this.inProgressStarted = System.currentTimeMillis();
    return this.inProgress;
  }
  
  protected long getMonitoringTimeOut() { return TimeUnit.SECONDS.toMillis(30L); }
  
  private static final Logger LOGGER = Logger.getLogger(AbstractNodeMonitorDescriptor.class.getName());
}
