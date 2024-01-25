package jenkins.slaves.systemInfo;

import hudson.Extension;
import org.jenkinsci.Symbol;

@Extension(ordinal = 1.0D)
@Symbol({"threadDump"})
public class ThreadDumpSlaveInfo extends SlaveSystemInfo {
  public String getDisplayName() { return Messages.ThreadDumpSlaveInfo_DisplayName(); }
}
