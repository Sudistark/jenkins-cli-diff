package jenkins.model;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Node;
import java.io.IOException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

@Extension(ordinal = 500.0D)
@Symbol({"builtInNode", "masterBuild"})
public class MasterBuildConfiguration extends GlobalConfiguration {
  public int getNumExecutors() { return Jenkins.get().getNumExecutors(); }
  
  public String getLabelString() { return Jenkins.get().getLabelString(); }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    Jenkins j = Jenkins.get();
    try {
      String num = json.getString("numExecutors");
      if (!num.matches("\\d+"))
        throw new Descriptor.FormException(Messages.Hudson_Computer_IncorrectNumberOfExecutors(), "numExecutors"); 
      j.setNumExecutors(json.getInt("numExecutors"));
      if (req.hasParameter("builtin.mode")) {
        j.setMode(Node.Mode.valueOf(req.getParameter("builtin.mode")));
      } else {
        j.setMode(Node.Mode.NORMAL);
      } 
      j.setLabelString(json.optString("labelString", ""));
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "numExecutors");
    } 
  }
}
