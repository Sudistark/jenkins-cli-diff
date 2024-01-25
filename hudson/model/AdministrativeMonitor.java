package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.ExtensionPoint.LegacyInstancesAreScopedToHudson;
import hudson.security.Permission;
import java.io.IOException;
import java.util.Set;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@LegacyInstancesAreScopedToHudson
public abstract class AdministrativeMonitor extends AbstractModelObject implements ExtensionPoint, StaplerProxy {
  public final String id;
  
  protected AdministrativeMonitor(String id) { this.id = id; }
  
  protected AdministrativeMonitor() { this.id = getClass().getName(); }
  
  public String getUrl() { return "administrativeMonitor/" + this.id; }
  
  public String getDisplayName() { return this.id; }
  
  public final String getSearchUrl() { return getUrl(); }
  
  public void disable(boolean value) throws IOException {
    Jenkins jenkins1 = Jenkins.get();
    Set<String> set = jenkins1.getDisabledAdministrativeMonitors();
    if (value) {
      set.add(this.id);
    } else {
      set.remove(this.id);
    } 
    jenkins1.setDisabledAdministrativeMonitors(set);
    jenkins1.save();
  }
  
  public boolean isEnabled() { return !Jenkins.get().getDisabledAdministrativeMonitors().contains(this.id); }
  
  public abstract boolean isActivated();
  
  public boolean isSecurity() { return false; }
  
  @RequirePOST
  public void doDisable(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    disable(true);
    rsp.sendRedirect2(req.getContextPath() + "/manage");
  }
  
  public Permission getRequiredPermission() { return Jenkins.ADMINISTER; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    Jenkins.get().checkPermission(getRequiredPermission());
    return this;
  }
  
  public static ExtensionList<AdministrativeMonitor> all() { return ExtensionList.lookup(AdministrativeMonitor.class); }
}
