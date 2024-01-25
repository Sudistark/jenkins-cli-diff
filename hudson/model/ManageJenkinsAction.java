package hudson.model;

import hudson.Extension;
import hudson.Util;
import hudson.security.Permission;
import java.io.IOException;
import jenkins.management.Badge;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithContextMenu;
import org.apache.commons.jelly.JellyException;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension(ordinal = 100.0D)
@Symbol({"manageJenkins"})
public class ManageJenkinsAction implements RootAction, StaplerFallback, ModelObjectWithContextMenu {
  public String getIconFileName() {
    if (Jenkins.get().hasAnyPermission(new Permission[] { Jenkins.MANAGE, Jenkins.SYSTEM_READ }))
      return "symbol-settings"; 
    return null;
  }
  
  public String getDisplayName() { return Messages.ManageJenkinsAction_DisplayName(); }
  
  public String getUrlName() { return "/manage"; }
  
  public Object getStaplerFallback() { return Jenkins.get(); }
  
  public ModelObjectWithContextMenu.ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws JellyException, IOException { return (new ModelObjectWithContextMenu.ContextMenu()).from(this, request, response, "index"); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void addContextMenuItem(ModelObjectWithContextMenu.ContextMenu menu, String url, String icon, String iconXml, String text, boolean post, boolean requiresConfirmation, Badge badge, String message) {
    if (Stapler.getCurrentRequest().findAncestorObject(getClass()) != null || !Util.isSafeToRedirectTo(url)) {
      menu.add(url, icon, iconXml, text, post, requiresConfirmation, badge, message);
      return;
    } 
    menu.add("manage/" + url, icon, iconXml, text, post, requiresConfirmation, badge, message);
  }
}
