package hudson.views;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.View;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 300.0D)
@Symbol({"defaultView"})
public class GlobalDefaultViewConfiguration extends GlobalConfiguration {
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    Jenkins j = Jenkins.get();
    if (json.has("primaryView")) {
      String viewName = json.getString("primaryView");
      View newPrimaryView = j.getView(viewName);
      if (newPrimaryView == null)
        throw new Descriptor.FormException(Messages.GlobalDefaultViewConfiguration_ViewDoesNotExist(viewName), "primaryView"); 
      j.setPrimaryView(newPrimaryView);
    } else {
      j.setPrimaryView((View)j.getViews().iterator().next());
    } 
    return true;
  }
}
