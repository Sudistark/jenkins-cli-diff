package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.model.PageDecorator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AdministrativeMonitorsDecorator extends PageDecorator {
  private final Collection<String> ignoredJenkinsRestOfUrls;
  
  public AdministrativeMonitorsDecorator() {
    this.ignoredJenkinsRestOfUrls = new ArrayList();
    this.ignoredJenkinsRestOfUrls.add("contextMenu");
    this.ignoredJenkinsRestOfUrls.add("configure");
  }
  
  @NonNull
  public String getDisplayName() { return Messages.AdministrativeMonitorsDecorator_DisplayName(); }
  
  public Collection<AdministrativeMonitor> filterNonSecurityAdministrativeMonitors(Collection<AdministrativeMonitor> activeMonitors) { return filterActiveAdministrativeMonitors(activeMonitors, false); }
  
  public Collection<AdministrativeMonitor> filterSecurityAdministrativeMonitors(Collection<AdministrativeMonitor> activeMonitors) { return filterActiveAdministrativeMonitors(activeMonitors, true); }
  
  private Collection<AdministrativeMonitor> filterActiveAdministrativeMonitors(Collection<AdministrativeMonitor> activeMonitors, boolean isSecurity) {
    Collection<AdministrativeMonitor> active = new ArrayList<AdministrativeMonitor>();
    for (AdministrativeMonitor am : activeMonitors) {
      if (am.isSecurity() == isSecurity)
        active.add(am); 
    } 
    return active;
  }
  
  public List<AdministrativeMonitor> getNonSecurityAdministrativeMonitors() {
    Collection<AdministrativeMonitor> allowedMonitors = getMonitorsToDisplay();
    if (allowedMonitors == null)
      return Collections.emptyList(); 
    return (List)allowedMonitors.stream()
      .filter(administrativeMonitor -> !administrativeMonitor.isSecurity())
      .collect(Collectors.toList());
  }
  
  public List<AdministrativeMonitor> getSecurityAdministrativeMonitors() {
    Collection<AdministrativeMonitor> allowedMonitors = getMonitorsToDisplay();
    if (allowedMonitors == null)
      return Collections.emptyList(); 
    return (List)allowedMonitors.stream()
      .filter(AdministrativeMonitor::isSecurity)
      .collect(Collectors.toList());
  }
  
  private Collection<AdministrativeMonitor> getAllActiveAdministrativeMonitors() {
    Collection<AdministrativeMonitor> active = new ArrayList<AdministrativeMonitor>();
    for (AdministrativeMonitor am : Jenkins.get().getActiveAdministrativeMonitors()) {
      if (am instanceof hudson.diagnosis.ReverseProxySetupMonitor)
        continue; 
      if (am instanceof jenkins.diagnostics.URICheckEncodingMonitor)
        continue; 
      active.add(am);
    } 
    return active;
  }
  
  public Collection<AdministrativeMonitor> getMonitorsToDisplay() {
    if (!Jenkins.get().hasPermission(Jenkins.SYSTEM_READ))
      return null; 
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req == null)
      return null; 
    List<Ancestor> ancestors = req.getAncestors();
    if (ancestors == null || ancestors.size() == 0)
      return null; 
    Ancestor a = (Ancestor)ancestors.get(ancestors.size() - 1);
    Object o = a.getObject();
    if (o instanceof hudson.util.HudsonIsLoading || o instanceof hudson.util.HudsonIsRestarting)
      return null; 
    if (o instanceof hudson.model.ManageJenkinsAction)
      return null; 
    if (o instanceof Jenkins) {
      String url = a.getRestOfUrl();
      if (this.ignoredJenkinsRestOfUrls.contains(url))
        return null; 
    } 
    return getAllActiveAdministrativeMonitors();
  }
}
