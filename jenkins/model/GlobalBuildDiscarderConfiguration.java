package jenkins.model;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Descriptor;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.List;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
@Symbol({"buildDiscarders"})
public class GlobalBuildDiscarderConfiguration extends GlobalConfiguration {
  private final DescribableList<GlobalBuildDiscarderStrategy, GlobalBuildDiscarderStrategyDescriptor> configuredBuildDiscarders;
  
  public static GlobalBuildDiscarderConfiguration get() { return (GlobalBuildDiscarderConfiguration)ExtensionList.lookupSingleton(GlobalBuildDiscarderConfiguration.class); }
  
  public GlobalBuildDiscarderConfiguration() {
    this
      .configuredBuildDiscarders = new DescribableList(this, List.of(new JobGlobalBuildDiscarderStrategy()));
    load();
  }
  
  private Object readResolve() {
    this.configuredBuildDiscarders.setOwner(this);
    return this;
  }
  
  public DescribableList<GlobalBuildDiscarderStrategy, GlobalBuildDiscarderStrategyDescriptor> getConfiguredBuildDiscarders() { return this.configuredBuildDiscarders; }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      this.configuredBuildDiscarders.rebuildHetero(req, json, GlobalBuildDiscarderStrategyDescriptor.all(), "configuredBuildDiscarders");
      return true;
    } catch (IOException x) {
      throw new Descriptor.FormException(x, "configuredBuildDiscarders");
    } 
  }
}
