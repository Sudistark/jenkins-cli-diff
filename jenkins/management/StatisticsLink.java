package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension(ordinal = 2.147482947E9D)
@Symbol({"loadStatistics"})
public class StatisticsLink extends ManagementLink {
  public String getIconFileName() { return "symbol-analytics"; }
  
  public String getDisplayName() { return Messages.StatisticsLink_DisplayName(); }
  
  public String getDescription() { return Messages.StatisticsLink_Description(); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.MANAGE; }
  
  public String getUrlName() { return "load-statistics"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.STATUS; }
}
