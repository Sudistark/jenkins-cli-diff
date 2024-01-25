package hudson.views;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String WeatherColumn_DisplayName() { return holder.format("WeatherColumn.DisplayName", new Object[0]); }
  
  public static Localizable _WeatherColumn_DisplayName() { return new Localizable(holder, "WeatherColumn.DisplayName", new Object[0]); }
  
  public static String LastSuccessColumn_DisplayName() { return holder.format("LastSuccessColumn.DisplayName", new Object[0]); }
  
  public static Localizable _LastSuccessColumn_DisplayName() { return new Localizable(holder, "LastSuccessColumn.DisplayName", new Object[0]); }
  
  public static String JobColumn_DisplayName() { return holder.format("JobColumn.DisplayName", new Object[0]); }
  
  public static Localizable _JobColumn_DisplayName() { return new Localizable(holder, "JobColumn.DisplayName", new Object[0]); }
  
  public static String LastDurationColumn_DisplayName() { return holder.format("LastDurationColumn.DisplayName", new Object[0]); }
  
  public static Localizable _LastDurationColumn_DisplayName() { return new Localizable(holder, "LastDurationColumn.DisplayName", new Object[0]); }
  
  public static String DefaultMyViewsTabsBar_DisplayName() { return holder.format("DefaultMyViewsTabsBar.DisplayName", new Object[0]); }
  
  public static Localizable _DefaultMyViewsTabsBar_DisplayName() { return new Localizable(holder, "DefaultMyViewsTabsBar.DisplayName", new Object[0]); }
  
  public static String GlobalDefaultViewConfiguration_ViewDoesNotExist(Object arg0) { return holder.format("GlobalDefaultViewConfiguration.ViewDoesNotExist", new Object[] { arg0 }); }
  
  public static Localizable _GlobalDefaultViewConfiguration_ViewDoesNotExist(Object arg0) { return new Localizable(holder, "GlobalDefaultViewConfiguration.ViewDoesNotExist", new Object[] { arg0 }); }
  
  public static String BuildButtonColumn_DisplayName() { return holder.format("BuildButtonColumn.DisplayName", new Object[0]); }
  
  public static Localizable _BuildButtonColumn_DisplayName() { return new Localizable(holder, "BuildButtonColumn.DisplayName", new Object[0]); }
  
  public static String LastStableColumn_DisplayName() { return holder.format("LastStableColumn.DisplayName", new Object[0]); }
  
  public static Localizable _LastStableColumn_DisplayName() { return new Localizable(holder, "LastStableColumn.DisplayName", new Object[0]); }
  
  public static String DefaultViewsTabsBar_DisplayName() { return holder.format("DefaultViewsTabsBar.DisplayName", new Object[0]); }
  
  public static Localizable _DefaultViewsTabsBar_DisplayName() { return new Localizable(holder, "DefaultViewsTabsBar.DisplayName", new Object[0]); }
  
  public static String LastFailureColumn_DisplayName() { return holder.format("LastFailureColumn.DisplayName", new Object[0]); }
  
  public static Localizable _LastFailureColumn_DisplayName() { return new Localizable(holder, "LastFailureColumn.DisplayName", new Object[0]); }
  
  public static String StatusColumn_DisplayName() { return holder.format("StatusColumn.DisplayName", new Object[0]); }
  
  public static Localizable _StatusColumn_DisplayName() { return new Localizable(holder, "StatusColumn.DisplayName", new Object[0]); }
  
  public static String StatusFilter_DisplayName() { return holder.format("StatusFilter.DisplayName", new Object[0]); }
  
  public static Localizable _StatusFilter_DisplayName() { return new Localizable(holder, "StatusFilter.DisplayName", new Object[0]); }
}
