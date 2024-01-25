package jenkins.triggers;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String ReverseBuildTrigger_build_after_other_projects_are_built() { return holder.format("ReverseBuildTrigger.build_after_other_projects_are_built", new Object[0]); }
  
  public static Localizable _ReverseBuildTrigger_build_after_other_projects_are_built() { return new Localizable(holder, "ReverseBuildTrigger.build_after_other_projects_are_built", new Object[0]); }
  
  public static String SCMTriggerItem_PollingVetoed(Object arg0) { return holder.format("SCMTriggerItem.PollingVetoed", new Object[] { arg0 }); }
  
  public static Localizable _SCMTriggerItem_PollingVetoed(Object arg0) { return new Localizable(holder, "SCMTriggerItem.PollingVetoed", new Object[] { arg0 }); }
  
  public static String ReverseBuildTrigger_running_as_cannot_even_see_for_trigger_f(Object arg0, Object arg1, Object arg2) { return holder.format("ReverseBuildTrigger.running_as_cannot_even_see_for_trigger_f", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _ReverseBuildTrigger_running_as_cannot_even_see_for_trigger_f(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "ReverseBuildTrigger.running_as_cannot_even_see_for_trigger_f", new Object[] { arg0, arg1, arg2 }); }
}
