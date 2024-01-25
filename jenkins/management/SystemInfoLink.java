package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147483147E9D)
@Symbol({"systemInfo"})
public class SystemInfoLink extends ManagementLink {
  public String getIconFileName() { return "symbol-server"; }
  
  public String getDisplayName() { return Messages.SystemInfoLink_DisplayName(); }
  
  public String getDescription() { return Messages.SystemInfoLink_Description(); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.READ; }
  
  public String getUrlName() { return "systemInfo"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.STATUS; }
}
