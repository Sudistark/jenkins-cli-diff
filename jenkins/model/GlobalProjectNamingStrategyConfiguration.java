package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.Permission;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 250.0D)
@Symbol({"projectNamingStrategy"})
public class GlobalProjectNamingStrategyConfiguration extends GlobalConfiguration {
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    Jenkins j = Jenkins.get();
    JSONObject optJSONObject = json.optJSONObject("useProjectNamingStrategy");
    if (optJSONObject != null) {
      JSONObject strategyObject = optJSONObject.getJSONObject("namingStrategy");
      String className = strategyObject.getString("$class");
      try {
        Class clazz = Class.forName(className, true, (j.getPluginManager()).uberClassLoader);
        ProjectNamingStrategy strategy = (ProjectNamingStrategy)req.bindJSON(clazz, strategyObject);
        j.setProjectNamingStrategy(strategy);
      } catch (ClassNotFoundException e) {
        throw new Descriptor.FormException(e, "namingStrategy");
      } 
    } 
    if (j.getProjectNamingStrategy() == null)
      j.setProjectNamingStrategy(ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY); 
    return true;
  }
  
  @NonNull
  public Permission getRequiredGlobalConfigPagePermission() { return Jenkins.MANAGE; }
}
