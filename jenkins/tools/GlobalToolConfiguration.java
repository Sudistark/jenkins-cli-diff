package jenkins.tools;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Functions;
import hudson.RestrictedSince;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import hudson.util.FormApply;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.management.Messages;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

@Extension(ordinal = 2.147483427E9D)
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class GlobalToolConfiguration extends ManagementLink {
  public String getIconFileName() { return "symbol-hammer"; }
  
  public String getDisplayName() { return Messages.ConfigureTools_DisplayName(); }
  
  public String getDescription() { return Messages.ConfigureTools_Description(); }
  
  public String getUrlName() { return "configureTools"; }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
  
  @POST
  public void doConfigure(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    boolean result = configure(req, req.getSubmittedForm());
    LOGGER.log(Level.FINE, "tools saved: " + result);
    FormApply.success(req.getContextPath() + "/manage").generateResponse(req, rsp, null);
  }
  
  private boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException, IOException {
    Jenkins j = Jenkins.get();
    j.checkPermission(Jenkins.ADMINISTER);
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
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.301")
  public static final Predicate<Descriptor> FILTER = input -> input.getCategory() instanceof ToolConfigurationCategory;
  
  private static final Logger LOGGER = Logger.getLogger(GlobalToolConfiguration.class.getName());
}
