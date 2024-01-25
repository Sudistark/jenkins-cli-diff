package jenkins.model;

import hudson.Extension;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.StructuredForm;
import hudson.model.Descriptor;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 100.0D)
@Symbol({"plugin"})
public class GlobalPluginConfiguration extends GlobalConfiguration {
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      for (JSONObject o : StructuredForm.toList(json, "plugin")) {
        String pluginName = o.getString("name");
        PluginWrapper pw = (Jenkins.get()).pluginManager.getPlugin(pluginName);
        Plugin p = (pw != null) ? pw.getPlugin() : null;
        if (p == null)
          throw new Descriptor.FormException("Cannot find the plugin instance: " + pluginName, "plugin"); 
        p.configure(req, o);
      } 
      return true;
    } catch (IOException|javax.servlet.ServletException e) {
      throw new Descriptor.FormException(e, "plugin");
    } 
  }
}
