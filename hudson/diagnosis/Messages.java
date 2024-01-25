package hudson.diagnosis;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String OldDataMonitor_DisplayName() { return holder.format("OldDataMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _OldDataMonitor_DisplayName() { return new Localizable(holder, "OldDataMonitor.DisplayName", new Object[0]); }
  
  public static String HsErrPidList_DisplayName() { return holder.format("HsErrPidList.DisplayName", new Object[0]); }
  
  public static Localizable _HsErrPidList_DisplayName() { return new Localizable(holder, "HsErrPidList.DisplayName", new Object[0]); }
  
  public static String OldDataMonitor_Description() { return holder.format("OldDataMonitor.Description", new Object[0]); }
  
  public static Localizable _OldDataMonitor_Description() { return new Localizable(holder, "OldDataMonitor.Description", new Object[0]); }
  
  public static String MemoryUsageMonitor_TOTAL() { return holder.format("MemoryUsageMonitor.TOTAL", new Object[0]); }
  
  public static Localizable _MemoryUsageMonitor_TOTAL() { return new Localizable(holder, "MemoryUsageMonitor.TOTAL", new Object[0]); }
  
  public static String ReverseProxySetupMonitor_DisplayName() { return holder.format("ReverseProxySetupMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _ReverseProxySetupMonitor_DisplayName() { return new Localizable(holder, "ReverseProxySetupMonitor.DisplayName", new Object[0]); }
  
  public static String TooManyJobsButNoView_DisplayName() { return holder.format("TooManyJobsButNoView.DisplayName", new Object[0]); }
  
  public static Localizable _TooManyJobsButNoView_DisplayName() { return new Localizable(holder, "TooManyJobsButNoView.DisplayName", new Object[0]); }
  
  public static String HudsonHomeDiskUsageMonitor_DisplayName() { return holder.format("HudsonHomeDiskUsageMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _HudsonHomeDiskUsageMonitor_DisplayName() { return new Localizable(holder, "HudsonHomeDiskUsageMonitor.DisplayName", new Object[0]); }
  
  public static String NullIdDescriptorMonitor_DisplayName() { return holder.format("NullIdDescriptorMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _NullIdDescriptorMonitor_DisplayName() { return new Localizable(holder, "NullIdDescriptorMonitor.DisplayName", new Object[0]); }
  
  public static String MemoryUsageMonitor_USED() { return holder.format("MemoryUsageMonitor.USED", new Object[0]); }
  
  public static Localizable _MemoryUsageMonitor_USED() { return new Localizable(holder, "MemoryUsageMonitor.USED", new Object[0]); }
  
  public static String OldDataMonitor_OldDataTooltip() { return holder.format("OldDataMonitor.OldDataTooltip", new Object[0]); }
  
  public static Localizable _OldDataMonitor_OldDataTooltip() { return new Localizable(holder, "OldDataMonitor.OldDataTooltip", new Object[0]); }
}
