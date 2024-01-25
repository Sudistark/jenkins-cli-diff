package jenkins.slaves.systemInfo;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String SystemPropertySlaveInfo_DisplayName() { return holder.format("SystemPropertySlaveInfo.DisplayName", new Object[0]); }
  
  public static Localizable _SystemPropertySlaveInfo_DisplayName() { return new Localizable(holder, "SystemPropertySlaveInfo.DisplayName", new Object[0]); }
  
  public static String EnvVarsSlaveInfo_DisplayName() { return holder.format("EnvVarsSlaveInfo.DisplayName", new Object[0]); }
  
  public static Localizable _EnvVarsSlaveInfo_DisplayName() { return new Localizable(holder, "EnvVarsSlaveInfo.DisplayName", new Object[0]); }
  
  public static String ClassLoaderStatisticsSlaveInfo_DisplayName() { return holder.format("ClassLoaderStatisticsSlaveInfo.DisplayName", new Object[0]); }
  
  public static Localizable _ClassLoaderStatisticsSlaveInfo_DisplayName() { return new Localizable(holder, "ClassLoaderStatisticsSlaveInfo.DisplayName", new Object[0]); }
  
  public static String ThreadDumpSlaveInfo_DisplayName() { return holder.format("ThreadDumpSlaveInfo.DisplayName", new Object[0]); }
  
  public static Localizable _ThreadDumpSlaveInfo_DisplayName() { return new Localizable(holder, "ThreadDumpSlaveInfo.DisplayName", new Object[0]); }
}
