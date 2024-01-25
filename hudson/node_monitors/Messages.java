package hudson.node_monitors;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String ResponseTimeMonitor_TimeOut(Object arg0) { return holder.format("ResponseTimeMonitor.TimeOut", new Object[] { arg0 }); }
  
  public static Localizable _ResponseTimeMonitor_TimeOut(Object arg0) { return new Localizable(holder, "ResponseTimeMonitor.TimeOut", new Object[] { arg0 }); }
  
  public static String ArchitectureMonitor_DisplayName() { return holder.format("ArchitectureMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _ArchitectureMonitor_DisplayName() { return new Localizable(holder, "ArchitectureMonitor.DisplayName", new Object[0]); }
  
  public static String DiskSpaceMonitorDescriptor_DiskSpace_FreeSpace(Object arg0, Object arg1) { return holder.format("DiskSpaceMonitorDescriptor.DiskSpace.FreeSpace", new Object[] { arg0, arg1 }); }
  
  public static Localizable _DiskSpaceMonitorDescriptor_DiskSpace_FreeSpace(Object arg0, Object arg1) { return new Localizable(holder, "DiskSpaceMonitorDescriptor.DiskSpace.FreeSpace", new Object[] { arg0, arg1 }); }
  
  public static String DiskSpaceMonitor_MarkedOnline(Object arg0) { return holder.format("DiskSpaceMonitor.MarkedOnline", new Object[] { arg0 }); }
  
  public static Localizable _DiskSpaceMonitor_MarkedOnline(Object arg0) { return new Localizable(holder, "DiskSpaceMonitor.MarkedOnline", new Object[] { arg0 }); }
  
  public static String DiskSpaceMonitor_DisplayName() { return holder.format("DiskSpaceMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _DiskSpaceMonitor_DisplayName() { return new Localizable(holder, "DiskSpaceMonitor.DisplayName", new Object[0]); }
  
  public static String ResponseTimeMonitor_MarkedOffline(Object arg0) { return holder.format("ResponseTimeMonitor.MarkedOffline", new Object[] { arg0 }); }
  
  public static Localizable _ResponseTimeMonitor_MarkedOffline(Object arg0) { return new Localizable(holder, "ResponseTimeMonitor.MarkedOffline", new Object[] { arg0 }); }
  
  public static String TemporarySpaceMonitor_DisplayName() { return holder.format("TemporarySpaceMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _TemporarySpaceMonitor_DisplayName() { return new Localizable(holder, "TemporarySpaceMonitor.DisplayName", new Object[0]); }
  
  public static String DiskSpaceMonitorDescriptor_DiskSpace_FreeSpaceTooLow(Object arg0, Object arg1) { return holder.format("DiskSpaceMonitorDescriptor.DiskSpace.FreeSpaceTooLow", new Object[] { arg0, arg1 }); }
  
  public static Localizable _DiskSpaceMonitorDescriptor_DiskSpace_FreeSpaceTooLow(Object arg0, Object arg1) { return new Localizable(holder, "DiskSpaceMonitorDescriptor.DiskSpace.FreeSpaceTooLow", new Object[] { arg0, arg1 }); }
  
  public static String ResponseTimeMonitor_DisplayName() { return holder.format("ResponseTimeMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _ResponseTimeMonitor_DisplayName() { return new Localizable(holder, "ResponseTimeMonitor.DisplayName", new Object[0]); }
  
  public static String ClockMonitor_DisplayName() { return holder.format("ClockMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _ClockMonitor_DisplayName() { return new Localizable(holder, "ClockMonitor.DisplayName", new Object[0]); }
  
  public static String AbstractNodeMonitorDescriptor_NoDataYet() { return holder.format("AbstractNodeMonitorDescriptor.NoDataYet", new Object[0]); }
  
  public static Localizable _AbstractNodeMonitorDescriptor_NoDataYet() { return new Localizable(holder, "AbstractNodeMonitorDescriptor.NoDataYet", new Object[0]); }
  
  public static String DiskSpaceMonitor_MarkedOffline(Object arg0) { return holder.format("DiskSpaceMonitor.MarkedOffline", new Object[] { arg0 }); }
  
  public static Localizable _DiskSpaceMonitor_MarkedOffline(Object arg0) { return new Localizable(holder, "DiskSpaceMonitor.MarkedOffline", new Object[] { arg0 }); }
  
  public static String SwapSpaceMonitor_DisplayName() { return holder.format("SwapSpaceMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _SwapSpaceMonitor_DisplayName() { return new Localizable(holder, "SwapSpaceMonitor.DisplayName", new Object[0]); }
  
  public static String MonitorMarkedNodeOffline_DisplayName() { return holder.format("MonitorMarkedNodeOffline.DisplayName", new Object[0]); }
  
  public static Localizable _MonitorMarkedNodeOffline_DisplayName() { return new Localizable(holder, "MonitorMarkedNodeOffline.DisplayName", new Object[0]); }
}
