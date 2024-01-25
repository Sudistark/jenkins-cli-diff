package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147483347E9D)
@Symbol({"reload"})
public class ReloadLink extends ManagementLink {
  public String getIconFileName() { return "symbol-reload"; }
  
  public String getDisplayName() { return Messages.ReloadLink_DisplayName(); }
  
  public String getDescription() { return Messages.ReloadLink_Description(); }
  
  public String getUrlName() { return "reload"; }
  
  public boolean getRequiresConfirmation() { return true; }
  
  public boolean getRequiresPOST() { return true; }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.MANAGE; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.TOOLS; }
}
