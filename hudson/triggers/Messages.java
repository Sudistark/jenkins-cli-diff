package hudson.triggers;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String TimerTrigger_DisplayName() { return holder.format("TimerTrigger.DisplayName", new Object[0]); }
  
  public static Localizable _TimerTrigger_DisplayName() { return new Localizable(holder, "TimerTrigger.DisplayName", new Object[0]); }
  
  public static String TimerTrigger_no_schedules_so_will_never_run() { return holder.format("TimerTrigger.no_schedules_so_will_never_run", new Object[0]); }
  
  public static Localizable _TimerTrigger_no_schedules_so_will_never_run() { return new Localizable(holder, "TimerTrigger.no_schedules_so_will_never_run", new Object[0]); }
  
  public static String SCMTrigger_BuildAction_DisplayName() { return holder.format("SCMTrigger.BuildAction.DisplayName", new Object[0]); }
  
  public static Localizable _SCMTrigger_BuildAction_DisplayName() { return new Localizable(holder, "SCMTrigger.BuildAction.DisplayName", new Object[0]); }
  
  public static String SCMTrigger_no_schedules_no_hooks() { return holder.format("SCMTrigger.no_schedules_no_hooks", new Object[0]); }
  
  public static Localizable _SCMTrigger_no_schedules_no_hooks() { return new Localizable(holder, "SCMTrigger.no_schedules_no_hooks", new Object[0]); }
  
  public static String TimerTrigger_would_last_have_run_at_would_next_run_at(Object arg0, Object arg1) { return holder.format("TimerTrigger.would_last_have_run_at_would_next_run_at", new Object[] { arg0, arg1 }); }
  
  public static Localizable _TimerTrigger_would_last_have_run_at_would_next_run_at(Object arg0, Object arg1) { return new Localizable(holder, "TimerTrigger.would_last_have_run_at_would_next_run_at", new Object[] { arg0, arg1 }); }
  
  public static String SlowTriggerAdminMonitor_DisplayName() { return holder.format("SlowTriggerAdminMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _SlowTriggerAdminMonitor_DisplayName() { return new Localizable(holder, "SlowTriggerAdminMonitor.DisplayName", new Object[0]); }
  
  public static String SCMTrigger_getDisplayName(Object arg0) { return holder.format("SCMTrigger.getDisplayName", new Object[] { arg0 }); }
  
  public static Localizable _SCMTrigger_getDisplayName(Object arg0) { return new Localizable(holder, "SCMTrigger.getDisplayName", new Object[] { arg0 }); }
  
  public static String TimerTrigger_MissingWhitespace() { return holder.format("TimerTrigger.MissingWhitespace", new Object[0]); }
  
  public static Localizable _TimerTrigger_MissingWhitespace() { return new Localizable(holder, "TimerTrigger.MissingWhitespace", new Object[0]); }
  
  public static String SCMTrigger_no_schedules_hooks() { return holder.format("SCMTrigger.no_schedules_hooks", new Object[0]); }
  
  public static Localizable _SCMTrigger_no_schedules_hooks() { return new Localizable(holder, "SCMTrigger.no_schedules_hooks", new Object[0]); }
  
  public static String SCMTrigger_SCMTriggerCause_ShortDescription() { return holder.format("SCMTrigger.SCMTriggerCause.ShortDescription", new Object[0]); }
  
  public static Localizable _SCMTrigger_SCMTriggerCause_ShortDescription() { return new Localizable(holder, "SCMTrigger.SCMTriggerCause.ShortDescription", new Object[0]); }
  
  public static String TimerTrigger_TimerTriggerCause_ShortDescription() { return holder.format("TimerTrigger.TimerTriggerCause.ShortDescription", new Object[0]); }
  
  public static Localizable _TimerTrigger_TimerTriggerCause_ShortDescription() { return new Localizable(holder, "TimerTrigger.TimerTriggerCause.ShortDescription", new Object[0]); }
  
  public static String SCMTrigger_DisplayName() { return holder.format("SCMTrigger.DisplayName", new Object[0]); }
  
  public static Localizable _SCMTrigger_DisplayName() { return new Localizable(holder, "SCMTrigger.DisplayName", new Object[0]); }
  
  public static String TimerTrigger_the_specified_cron_tab_is_rare_or_impossible() { return holder.format("TimerTrigger.the_specified_cron_tab_is_rare_or_impossible", new Object[0]); }
  
  public static Localizable _TimerTrigger_the_specified_cron_tab_is_rare_or_impossible() { return new Localizable(holder, "TimerTrigger.the_specified_cron_tab_is_rare_or_impossible", new Object[0]); }
  
  public static String SCMTrigger_AdministrativeMonitorImpl_DisplayName() { return holder.format("SCMTrigger.AdministrativeMonitorImpl.DisplayName", new Object[0]); }
  
  public static Localizable _SCMTrigger_AdministrativeMonitorImpl_DisplayName() { return new Localizable(holder, "SCMTrigger.AdministrativeMonitorImpl.DisplayName", new Object[0]); }
  
  public static String Trigger_init() { return holder.format("Trigger.init", new Object[0]); }
  
  public static Localizable _Trigger_init() { return new Localizable(holder, "Trigger.init", new Object[0]); }
}
