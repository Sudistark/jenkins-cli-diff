package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import java.net.URL;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Symbol({"about"})
public class AboutJenkins extends ManagementLink {
  public String getIconFileName() { return "symbol-jenkins"; }
  
  public String getUrlName() { return "about"; }
  
  public String getDisplayName() { return Messages.AboutJenkins_DisplayName(); }
  
  public String getDescription() { return Messages.AboutJenkins_Description(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public URL getLicensesURL() { return AboutJenkins.class.getResource("/META-INF/licenses.xml"); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.READ; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.STATUS; }
}
