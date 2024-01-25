package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.RestrictedSince;
import hudson.model.RootAction;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Symbol({"cloud"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Deprecated
@RestrictedSince("2.205")
public class GlobalCloudConfiguration implements RootAction {
  @CheckForNull
  public String getIconFileName() { return null; }
  
  @CheckForNull
  public String getDisplayName() { return Messages.GlobalCloudConfiguration_DisplayName(); }
  
  public String getUrlName() { return "configureClouds"; }
}
