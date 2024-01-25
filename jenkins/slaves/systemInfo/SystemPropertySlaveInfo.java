package jenkins.slaves.systemInfo;

import hudson.Extension;
import hudson.model.Computer;
import hudson.security.Permission;
import org.jenkinsci.Symbol;

@Extension(ordinal = 3.0D)
@Symbol({"systemProperties"})
public class SystemPropertySlaveInfo extends SlaveSystemInfo {
  public String getDisplayName() { return Messages.SystemPropertySlaveInfo_DisplayName(); }
  
  public Permission getRequiredPermission() { return Computer.EXTENDED_READ; }
}
