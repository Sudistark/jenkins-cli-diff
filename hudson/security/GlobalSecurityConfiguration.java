package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.BulkChange;
import hudson.Extension;
import hudson.Functions;
import hudson.RestrictedSince;
import hudson.markup.MarkupFormatter;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.ServerTcpPort;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

@Extension(ordinal = 2.147483437E9D)
@Symbol({"securityConfig"})
public class GlobalSecurityConfiguration extends ManagementLink implements Describable<GlobalSecurityConfiguration> {
  private static final Logger LOGGER = Logger.getLogger(GlobalSecurityConfiguration.class.getName());
  
  public SecurityRealm getSecurityRealm() { return Jenkins.get().getSecurityRealm(); }
  
  public AuthorizationStrategy getAuthorizationStrategy() { return Jenkins.get().getAuthorizationStrategy(); }
  
  public MarkupFormatter getMarkupFormatter() { return Jenkins.get().getMarkupFormatter(); }
  
  public int getSlaveAgentPort() { return Jenkins.get().getSlaveAgentPort(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isSlaveAgentPortEnforced() { return Jenkins.get().isSlaveAgentPortEnforced(); }
  
  @NonNull
  public Set<String> getAgentProtocols() { return Jenkins.get().getAgentProtocols(); }
  
  public boolean isDisableRememberMe() { return Jenkins.get().isDisableRememberMe(); }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.SECURITY; }
  
  @POST
  public void doConfigure(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    JSONObject json = req.getSubmittedForm();
    bc = new BulkChange(Jenkins.get());
    try {
      boolean result = configure(req, json);
      LOGGER.log(Level.FINE, "security saved: " + result);
      Jenkins.get().save();
      FormApply.success(req.getContextPath() + "/manage").generateResponse(req, rsp, null);
    } catch (JSONException x) {
      LOGGER.warning(() -> "Bad JSON:\n" + json.toString(2));
      throw x;
    } finally {
      bc.commit();
    } 
  }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    Jenkins j = Jenkins.get();
    j.checkPermission(Jenkins.ADMINISTER);
    j.setDisableRememberMe(json.optBoolean("disableRememberMe", false));
    j.setSecurityRealm((SecurityRealm)Descriptor.bindJSON(req, SecurityRealm.class, json.getJSONObject("securityRealm")));
    j.setAuthorizationStrategy((AuthorizationStrategy)Descriptor.bindJSON(req, AuthorizationStrategy.class, json.getJSONObject("authorizationStrategy")));
    if (json.has("markupFormatter")) {
      j.setMarkupFormatter((MarkupFormatter)req.bindJSON(MarkupFormatter.class, json.getJSONObject("markupFormatter")));
    } else {
      j.setMarkupFormatter(null);
    } 
    if (!isSlaveAgentPortEnforced())
      try {
        j.setSlaveAgentPort((new ServerTcpPort(json.getJSONObject("slaveAgentPort"))).getPort());
      } catch (IOException e) {
        throw new Descriptor.FormException(e, "slaveAgentPortType");
      }  
    Set<String> agentProtocols = new TreeSet<String>();
    if (json.has("agentProtocol")) {
      Object protocols = json.get("agentProtocol");
      if (protocols instanceof JSONArray) {
        for (int i = 0; i < ((JSONArray)protocols).size(); i++)
          agentProtocols.add(((JSONArray)protocols).getString(i)); 
      } else {
        agentProtocols.add(protocols.toString());
      } 
    } 
    j.setAgentProtocols(agentProtocols);
    boolean result = true;
    for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigByDescriptor(FILTER))
      result &= configureDescriptor(req, json, d); 
    return result;
  }
  
  private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws Descriptor.FormException {
    String name = d.getJsonSafeClassName();
    JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject();
    json.putAll(js);
    return d.configure(req, js);
  }
  
  public String getDisplayName() { return getDescriptor().getDisplayName(); }
  
  public String getDescription() { return Messages.GlobalSecurityConfiguration_Description(); }
  
  public String getIconFileName() { return "symbol-lock-closed"; }
  
  public String getUrlName() { return "configureSecurity"; }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.222")
  public static final Predicate<Descriptor> FILTER = input -> input.getCategory() instanceof jenkins.model.GlobalConfigurationCategory.Security;
  
  public Descriptor<GlobalSecurityConfiguration> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
}
