package hudson.scheduler;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String BaseParser_OutOfRange(Object arg0, Object arg1, Object arg2) { return holder.format("BaseParser.OutOfRange", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _BaseParser_OutOfRange(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "BaseParser.OutOfRange", new Object[] { arg0, arg1, arg2 }); }
  
  public static String BaseParser_StartEndReversed(Object arg0, Object arg1) { return holder.format("BaseParser.StartEndReversed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _BaseParser_StartEndReversed(Object arg0, Object arg1) { return new Localizable(holder, "BaseParser.StartEndReversed", new Object[] { arg0, arg1 }); }
  
  public static String CronTab_spread_load_evenly_by_using_rather_than_(Object arg0, Object arg1) { return holder.format("CronTab.spread_load_evenly_by_using_rather_than_", new Object[] { arg0, arg1 }); }
  
  public static Localizable _CronTab_spread_load_evenly_by_using_rather_than_(Object arg0, Object arg1) { return new Localizable(holder, "CronTab.spread_load_evenly_by_using_rather_than_", new Object[] { arg0, arg1 }); }
  
  public static String CronTab_short_cycles_in_the_day_of_month_field_w() { return holder.format("CronTab.short_cycles_in_the_day_of_month_field_w", new Object[0]); }
  
  public static Localizable _CronTab_short_cycles_in_the_day_of_month_field_w() { return new Localizable(holder, "CronTab.short_cycles_in_the_day_of_month_field_w", new Object[0]); }
  
  public static String BaseParser_MustBePositive(Object arg0) { return holder.format("BaseParser.MustBePositive", new Object[] { arg0 }); }
  
  public static Localizable _BaseParser_MustBePositive(Object arg0) { return new Localizable(holder, "BaseParser.MustBePositive", new Object[] { arg0 }); }
  
  public static String CronTab_do_you_really_mean_every_minute_when_you(Object arg0, Object arg1) { return holder.format("CronTab.do_you_really_mean_every_minute_when_you", new Object[] { arg0, arg1 }); }
  
  public static Localizable _CronTab_do_you_really_mean_every_minute_when_you(Object arg0, Object arg1) { return new Localizable(holder, "CronTab.do_you_really_mean_every_minute_when_you", new Object[] { arg0, arg1 }); }
  
  public static String CronTabList_InvalidInput(Object arg0, Object arg1) { return holder.format("CronTabList.InvalidInput", new Object[] { arg0, arg1 }); }
  
  public static Localizable _CronTabList_InvalidInput(Object arg0, Object arg1) { return new Localizable(holder, "CronTabList.InvalidInput", new Object[] { arg0, arg1 }); }
}
