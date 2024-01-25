package jenkins.security;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.model.AdministrativeMonitor;
import hudson.model.UpdateSite;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UpdateSiteWarningsMonitor extends AdministrativeMonitor {
  public boolean isActivated() {
    if (!Jenkins.get().getUpdateCenter().isSiteDataReady())
      return false; 
    return (!getActiveCoreWarnings().isEmpty() || !getActivePluginWarningsByPlugin().isEmpty());
  }
  
  public boolean isSecurity() { return true; }
  
  public List<UpdateSite.Warning> getActiveCoreWarnings() {
    List<UpdateSite.Warning> CoreWarnings = new ArrayList<UpdateSite.Warning>();
    for (UpdateSite.Warning warning : getActiveWarnings()) {
      if (warning.type != UpdateSite.WarningType.CORE)
        continue; 
      CoreWarnings.add(warning);
    } 
    return CoreWarnings;
  }
  
  public Map<PluginWrapper, List<UpdateSite.Warning>> getActivePluginWarningsByPlugin() {
    Map<PluginWrapper, List<UpdateSite.Warning>> activePluginWarningsByPlugin = new HashMap<PluginWrapper, List<UpdateSite.Warning>>();
    for (UpdateSite.Warning warning : getActiveWarnings()) {
      if (warning.type != UpdateSite.WarningType.PLUGIN)
        continue; 
      String pluginName = warning.component;
      PluginWrapper plugin = Jenkins.get().getPluginManager().getPlugin(pluginName);
      if (!activePluginWarningsByPlugin.containsKey(plugin))
        activePluginWarningsByPlugin.put(plugin, new ArrayList()); 
      ((List)activePluginWarningsByPlugin.get(plugin)).add(warning);
    } 
    return activePluginWarningsByPlugin;
  }
  
  private Set<UpdateSite.Warning> getActiveWarnings() {
    UpdateSiteWarningsConfiguration configuration = (UpdateSiteWarningsConfiguration)ExtensionList.lookupSingleton(UpdateSiteWarningsConfiguration.class);
    HashSet<UpdateSite.Warning> activeWarnings = new HashSet<UpdateSite.Warning>();
    for (UpdateSite.Warning warning : configuration.getApplicableWarnings()) {
      if (!configuration.getIgnoredWarnings().contains(warning.id))
        activeWarnings.add(warning); 
    } 
    return Collections.unmodifiableSet(activeWarnings);
  }
  
  @RequirePOST
  public HttpResponse doForward(@QueryParameter String fix, @QueryParameter String configure) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (fix != null)
      return HttpResponses.redirectViaContextPath("pluginManager"); 
    if (configure != null)
      return HttpResponses.redirectViaContextPath("configureSecurity"); 
    return HttpResponses.redirectViaContextPath("/");
  }
  
  public boolean hasApplicableHiddenWarnings() {
    UpdateSiteWarningsConfiguration configuration = (UpdateSiteWarningsConfiguration)ExtensionList.lookupSingleton(UpdateSiteWarningsConfiguration.class);
    return (getActiveWarnings().size() < configuration.getApplicableWarnings().size());
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  public String getDisplayName() { return Messages.UpdateSiteWarningsMonitor_DisplayName(); }
}
