package hudson.node_monitors;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;

@Extension
public class MonitorMarkedNodeOffline extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.MonitorMarkedNodeOffline_DisplayName(); }
  
  public boolean active = false;
  
  public boolean isActivated() { return this.active; }
}
