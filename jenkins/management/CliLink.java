package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147482847E9D)
@Symbol({"cli"})
public class CliLink extends ManagementLink {
  public String getIconFileName() { return "symbol-terminal"; }
  
  public String getDisplayName() { return Messages.CliLink_DisplayName(); }
  
  public String getDescription() { return Messages.CliLink_Description(); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.READ; }
  
  public String getUrlName() { return "cli"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.TOOLS; }
}
