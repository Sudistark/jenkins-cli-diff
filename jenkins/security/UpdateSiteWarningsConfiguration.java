package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.model.Descriptor;
import hudson.model.PersistentDescriptor;
import hudson.model.UpdateSite;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UpdateSiteWarningsConfiguration extends GlobalConfiguration implements PersistentDescriptor {
  private HashSet<String> ignoredWarnings = new HashSet();
  
  @NonNull
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class); }
  
  @NonNull
  public Set<String> getIgnoredWarnings() { return Collections.unmodifiableSet(this.ignoredWarnings); }
  
  @DataBoundSetter
  public void setIgnoredWarnings(Set<String> ignoredWarnings) { this.ignoredWarnings = new HashSet(ignoredWarnings); }
  
  public boolean isIgnored(@NonNull UpdateSite.Warning warning) { return this.ignoredWarnings.contains(warning.id); }
  
  @CheckForNull
  public PluginWrapper getPlugin(@NonNull UpdateSite.Warning warning) {
    if (warning.type != UpdateSite.WarningType.PLUGIN)
      return null; 
    return Jenkins.get().getPluginManager().getPlugin(warning.component);
  }
  
  @NonNull
  public Set<UpdateSite.Warning> getAllWarnings() {
    HashSet<UpdateSite.Warning> allWarnings = new HashSet<UpdateSite.Warning>();
    for (UpdateSite site : Jenkins.get().getUpdateCenter().getSites()) {
      UpdateSite.Data data = site.getData();
      if (data != null)
        allWarnings.addAll(data.getWarnings()); 
    } 
    return allWarnings;
  }
  
  @NonNull
  public Set<UpdateSite.Warning> getApplicableWarnings() {
    Set<UpdateSite.Warning> allWarnings = getAllWarnings();
    HashSet<UpdateSite.Warning> applicableWarnings = new HashSet<UpdateSite.Warning>();
    for (UpdateSite.Warning warning : allWarnings) {
      if (warning.isRelevant())
        applicableWarnings.add(warning); 
    } 
    return Collections.unmodifiableSet(applicableWarnings);
  }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    HashSet<String> newIgnoredWarnings = new HashSet<String>();
    for (Object key : json.keySet()) {
      String warningKey = key.toString();
      if (!json.getBoolean(warningKey))
        newIgnoredWarnings.add(warningKey); 
    } 
    this.ignoredWarnings = newIgnoredWarnings;
    save();
    return true;
  }
}
