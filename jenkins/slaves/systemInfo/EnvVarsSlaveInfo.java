package jenkins.slaves.systemInfo;

import hudson.Extension;
import hudson.model.Computer;
import hudson.security.Permission;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.0D)
@Symbol({"envVars"})
public class EnvVarsSlaveInfo extends SlaveSystemInfo {
  public String getDisplayName() { return Messages.EnvVarsSlaveInfo_DisplayName(); }
  
  public Permission getRequiredPermission() { return Computer.EXTENDED_READ; }
}
