package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147482647E9D)
@Symbol({"nodes"})
public class NodesLink extends ManagementLink {
  public String getIconFileName() { return "symbol-computer"; }
  
  public String getDisplayName() { return Messages.NodesLink_DisplayName(); }
  
  public String getDescription() { return Messages.NodesLink_Description(); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.READ; }
  
  public String getUrlName() { return "computer"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
}
