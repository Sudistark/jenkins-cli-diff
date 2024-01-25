package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147483047E9D)
@Symbol({"log"})
public class SystemLogLink extends ManagementLink {
  public String getIconFileName() { return "symbol-journal"; }
  
  public String getDisplayName() { return Messages.SystemLogLink_DisplayName(); }
  
  public String getDescription() { return Messages.SystemLogLink_Description(); }
  
  public String getUrlName() { return "log"; }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.STATUS; }
}
