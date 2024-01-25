package jenkins.appearance;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Functions;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

@Extension
public class AppearanceGlobalConfiguration extends ManagementLink {
  private static final Logger LOGGER = Logger.getLogger(AppearanceGlobalConfiguration.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final Predicate<Descriptor> FILTER = input -> input.getCategory() instanceof AppearanceCategory;
  
  public String getIconFileName() {
    if (Functions.getSortedDescriptorsForGlobalConfigByDescriptor(FILTER).isEmpty())
      return null; 
    return "symbol-brush-outline";
  }
  
  public String getDisplayName() { return Messages.AppearanceGlobalConfiguration_DisplayName(); }
  
  public String getDescription() { return Messages.AppearanceGlobalConfiguration_Description(); }
  
  public String getUrlName() { return "appearance"; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
  
  @POST
  public void doConfigure(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    boolean result = configure(req, req.getSubmittedForm());
    LOGGER.log(Level.FINE, "appearance saved: " + result);
    FormApply.success(req.getContextPath() + "/manage").generateResponse(req, rsp, null);
  }
  
  private boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException, IOException {
    Jenkins j = Jenkins.get();
    j.checkPermission(Jenkins.MANAGE);
    boolean result = true;
    for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigByDescriptor(FILTER))
      result &= configureDescriptor(req, json, d); 
    j.save();
    return result;
  }
  
  private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws Descriptor.FormException {
    String name = d.getJsonSafeClassName();
    JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject();
    json.putAll(js);
    return d.configure(req, js);
  }
}
