package jenkins.slaves.systemInfo;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Computer;
import hudson.security.Permission;

public abstract class SlaveSystemInfo implements ExtensionPoint {
  public abstract String getDisplayName();
  
  public static ExtensionList<SlaveSystemInfo> all() { return ExtensionList.lookup(SlaveSystemInfo.class); }
  
  public Permission getRequiredPermission() { return Computer.CONNECT; }
}
