package jenkins.slaves.systemInfo;

import hudson.Extension;
import hudson.model.Computer;
import hudson.security.Permission;
import org.jenkinsci.Symbol;

@Extension(ordinal = 0.0D)
@Symbol({"classLoaderStatistics"})
public class ClassLoaderStatisticsSlaveInfo extends SlaveSystemInfo {
  public String getDisplayName() { return Messages.ClassLoaderStatisticsSlaveInfo_DisplayName(); }
  
  public Permission getRequiredPermission() { return Computer.EXTENDED_READ; }
}
