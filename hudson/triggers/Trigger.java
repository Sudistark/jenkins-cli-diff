package hudson.triggers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DependencyRunner;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.scheduler.CronTabList;
import hudson.scheduler.Hash;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.triggers.TriggeredItem;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

public abstract class Trigger<J extends Item> extends Object implements Describable<Trigger<?>>, ExtensionPoint {
  protected final String spec;
  
  protected CronTabList tabs;
  
  @CheckForNull
  protected J job;
  
  private static Future previousSynchronousPolling;
  
  public void start(J project, boolean newInstance) {
    LOGGER.finer(() -> "Starting " + this + " on " + project);
    this.job = project;
    try {
      if (this.spec != null) {
        this.tabs = CronTabList.create(this.spec, Hash.from(project.getFullName()));
      } else {
        LOGGER.log(Level.WARNING, "The job {0} has a null crontab spec which is incorrect", this.job.getFullName());
      } 
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, String.format("Failed to parse crontab spec %s in job %s", new Object[] { this.spec, project.getFullName() }), e);
    } 
  }
  
  public void run() {}
  
  public void stop() {}
  
  @Deprecated
  public Action getProjectAction() { return null; }
  
  public Collection<? extends Action> getProjectActions() {
    Action a = getProjectAction();
    if (a == null)
      return Collections.emptyList(); 
    return List.of(a);
  }
  
  public TriggerDescriptor getDescriptor() { return (TriggerDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  protected Trigger(@NonNull String cronTabSpec) {
    this.spec = cronTabSpec;
    this.tabs = CronTabList.create(cronTabSpec);
  }
  
  protected Trigger() {
    this.spec = "";
    this.tabs = new CronTabList(Collections.emptyList());
  }
  
  public final String getSpec() { return this.spec; }
  
  protected Object readResolve() throws ObjectStreamException {
    try {
      this.tabs = CronTabList.create(this.spec);
    } catch (IllegalArgumentException e) {
      InvalidObjectException x = new InvalidObjectException(e.getMessage());
      x.initCause(e);
      throw x;
    } 
    return this;
  }
  
  public String toString() {
    return super.toString() + "[" + super.toString() + "]";
  }
  
  public static void checkTriggers(Calendar cal) {
    Jenkins inst = Jenkins.get();
    SCMTrigger.DescriptorImpl scmd = (SCMTrigger.DescriptorImpl)inst.getDescriptorByType(SCMTrigger.DescriptorImpl.class);
    if (scmd.synchronousPolling) {
      LOGGER.fine("using synchronous polling");
      if (previousSynchronousPolling == null || previousSynchronousPolling.isDone()) {
        previousSynchronousPolling = scmd.getExecutor().submit(new DependencyRunner(new Object()));
      } else {
        LOGGER.fine("synchronous polling has detected unfinished jobs, will not trigger additional jobs.");
      } 
    } 
    for (TriggeredItem p : inst.allItems(TriggeredItem.class)) {
      LOGGER.finer(() -> "considering " + p);
      for (Trigger t : p.getTriggers().values()) {
        LOGGER.finer(() -> "found trigger " + t);
        if (!(p instanceof hudson.model.AbstractProject) || !(t instanceof SCMTrigger) || !scmd.synchronousPolling) {
          if (t != null && t.spec != null && t.tabs != null) {
            LOGGER.log(Level.FINE, "cron checking {0} with spec ‘{1}’", new Object[] { p, t.spec.trim() });
            if (t.tabs.check(cal)) {
              LOGGER.log(Level.CONFIG, "cron triggered {0}", p);
              try {
                long begin_time = System.currentTimeMillis();
                if (t.job == null)
                  LOGGER.fine(() -> "" + t + " not yet started on " + t + " but trying to run anyway"); 
                t.run();
                long end_time = System.currentTimeMillis();
                if (end_time - begin_time > CRON_THRESHOLD * 1000L) {
                  TriggerDescriptor descriptor = t.getDescriptor();
                  String name = descriptor.getDisplayName();
                  String msg = String.format("Trigger '%s' triggered by '%s' (%s) spent too much time (%s) in its execution, other timers could be delayed.", new Object[] { name, p
                        .getFullDisplayName(), p.getFullName(), Util.getTimeSpanString(end_time - begin_time) });
                  LOGGER.log(Level.WARNING, msg);
                  SlowTriggerAdminMonitor.getInstance().report(descriptor.getClass(), p.getFullName(), end_time - begin_time);
                } 
                continue;
              } catch (Throwable e) {
                LOGGER.log(Level.WARNING, t.getClass().getName() + ".run() failed for " + t.getClass().getName(), e);
                continue;
              } 
            } 
            LOGGER.log(Level.FINER, "did not trigger {0}", p);
            continue;
          } 
          LOGGER.log(Level.WARNING, "The job {0} has a syntactically incorrect config and is missing the cron spec for a trigger", p.getFullName());
        } 
      } 
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  @RestrictedSince("2.289")
  public static long CRON_THRESHOLD = SystemProperties.getLong(Trigger.class.getName() + ".CRON_THRESHOLD", Long.valueOf(30L)).longValue();
  
  private static final Logger LOGGER = Logger.getLogger(Trigger.class.getName());
  
  @Deprecated
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "for backward compatibility")
  @CheckForNull
  public static Timer timer;
  
  public static DescriptorExtensionList<Trigger<?>, TriggerDescriptor> all() { return Jenkins.get().getDescriptorList(Trigger.class); }
  
  public static List<TriggerDescriptor> for_(Item i) {
    List<TriggerDescriptor> r = new ArrayList<TriggerDescriptor>();
    for (TriggerDescriptor t : all()) {
      if (!t.isApplicable(i))
        continue; 
      if (i instanceof TopLevelItem) {
        TopLevelItemDescriptor tld = ((TopLevelItem)i).getDescriptor();
        if (tld != null && !tld.isApplicable(t))
          continue; 
      } 
      r.add(t);
    } 
    return r;
  }
}
