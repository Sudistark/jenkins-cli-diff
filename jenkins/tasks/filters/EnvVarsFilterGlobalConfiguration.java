package jenkins.tasks.filters;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Descriptor;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol({"envVarsFilter"})
@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public class EnvVarsFilterGlobalConfiguration extends GlobalConfiguration {
  private DescribableList<EnvVarsFilterGlobalRule, Descriptor<EnvVarsFilterGlobalRule>> activatedGlobalRules;
  
  public EnvVarsFilterGlobalConfiguration() {
    this.activatedGlobalRules = new DescribableList(this);
    load();
  }
  
  public static EnvVarsFilterGlobalConfiguration get() { return (EnvVarsFilterGlobalConfiguration)GlobalConfiguration.all().get(EnvVarsFilterGlobalConfiguration.class); }
  
  public static ExtensionList<Descriptor<EnvVarsFilterGlobalRule>> getAllGlobalRules() { return Jenkins.get().getDescriptorList(EnvVarsFilterGlobalRule.class); }
  
  public static List<EnvVarsFilterGlobalRule> getAllActivatedGlobalRules() { return (get()).activatedGlobalRules; }
  
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Unclassified.class); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      this.activatedGlobalRules.rebuildHetero(req, json, getAllGlobalRules(), "rules");
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "rules");
    } 
    save();
    return true;
  }
}
