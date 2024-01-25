package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.model.UpdateCenter;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147483247E9D)
@Symbol({"plugins"})
public class PluginsLink extends ManagementLink {
  public String getIconFileName() { return "plugin.svg"; }
  
  public String getDisplayName() { return Messages.PluginsLink_DisplayName(); }
  
  public String getDescription() { return Messages.PluginsLink_Description(); }
  
  public String getUrlName() { return "pluginManager"; }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
  
  public Badge getBadge() {
    UpdateCenter updateCenter = Jenkins.get().getUpdateCenter();
    return updateCenter.getBadge();
  }
}
