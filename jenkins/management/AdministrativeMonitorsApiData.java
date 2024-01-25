package jenkins.management;

import hudson.model.AdministrativeMonitor;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AdministrativeMonitorsApiData {
  private final List<AdministrativeMonitor> monitorsList;
  
  AdministrativeMonitorsApiData(List<AdministrativeMonitor> monitors) {
    this.monitorsList = new ArrayList();
    this.monitorsList.addAll(monitors);
  }
  
  public List<AdministrativeMonitor> getMonitorsList() { return this.monitorsList; }
  
  public boolean hasActiveMonitors() { return !this.monitorsList.isEmpty(); }
}
