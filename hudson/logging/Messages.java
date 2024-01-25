package hudson.logging;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String LogRecorder_Target_Empty_Warning() { return holder.format("LogRecorder.Target.Empty.Warning", new Object[0]); }
  
  public static Localizable _LogRecorder_Target_Empty_Warning() { return new Localizable(holder, "LogRecorder.Target.Empty.Warning", new Object[0]); }
  
  public static String LogRecorderManager_DisplayName() { return holder.format("LogRecorderManager.DisplayName", new Object[0]); }
  
  public static Localizable _LogRecorderManager_DisplayName() { return new Localizable(holder, "LogRecorderManager.DisplayName", new Object[0]); }
  
  public static String LogRecorderManager_LoggerNotFound(Object arg0) { return holder.format("LogRecorderManager.LoggerNotFound", new Object[] { arg0 }); }
  
  public static Localizable _LogRecorderManager_LoggerNotFound(Object arg0) { return new Localizable(holder, "LogRecorderManager.LoggerNotFound", new Object[] { arg0 }); }
  
  public static String LogRecorderManager_init() { return holder.format("LogRecorderManager.init", new Object[0]); }
  
  public static Localizable _LogRecorderManager_init() { return new Localizable(holder, "LogRecorderManager.init", new Object[0]); }
}
