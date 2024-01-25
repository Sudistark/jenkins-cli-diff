package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147482747E9D)
@Symbol({"console"})
public class ConsoleLink extends ManagementLink {
  public String getIconFileName() { return "symbol-code-working"; }
  
  public String getDisplayName() { return Messages.ConsoleLink_DisplayName(); }
  
  public String getDescription() { return Messages.ConsoleLink_Description(); }
  
  public String getUrlName() { return "script"; }
  
  public Permission getRequiredPermission() { return Jenkins.ADMINISTER; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.TOOLS; }
}
