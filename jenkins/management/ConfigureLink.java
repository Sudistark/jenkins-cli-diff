package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147483447E9D)
@Symbol({"configure"})
public class ConfigureLink extends ManagementLink {
  public String getIconFileName() { return "symbol-settings"; }
  
  public String getDisplayName() { return Messages.ConfigureLink_DisplayName(); }
  
  public String getDescription() { return Messages.ConfigureLink_Description(); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.READ; }
  
  public String getUrlName() { return "configure"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
}
