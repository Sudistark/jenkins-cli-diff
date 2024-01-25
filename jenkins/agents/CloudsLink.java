package jenkins.agents;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"clouds"})
public class CloudsLink extends ManagementLink {
  public String getDisplayName() { return Messages.CloudsLink_DisplayName(); }
  
  public String getDescription() { return Messages.CloudsLink_Description(); }
  
  public String getIconFileName() { return "symbol-cloud"; }
  
  public String getUrlName() { return "cloud"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
}
