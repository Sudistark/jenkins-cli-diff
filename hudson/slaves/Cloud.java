package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Actionable;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionScope;
import hudson.util.DescriptorList;
import hudson.util.FormApply;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import javax.servlet.ServletException;
import jenkins.agents.Messages;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.Validate;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

public abstract class Cloud extends Actionable implements ExtensionPoint, Describable<Cloud>, AccessControlled {
  public String name;
  
  protected Cloud(String name) {
    Validate.notEmpty(name, Messages.Cloud_RequiredName());
    this.name = name;
  }
  
  public String getDisplayName() { return this.name; }
  
  @NonNull
  public String getUrl() { return "cloud/" + Util.rawEncode(this.name) + "/"; }
  
  @NonNull
  public String getSearchUrl() { return getUrl(); }
  
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  @Deprecated
  public Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
    return (Collection)Util.ifOverridden(() -> provision(new CloudState(label, 0), excessWorkload), Cloud.class, 
        
        getClass(), "provision", new Class[] { CloudState.class, int.class });
  }
  
  public Collection<NodeProvisioner.PlannedNode> provision(CloudState state, int excessWorkload) { return provision(state.getLabel(), excessWorkload); }
  
  @Deprecated
  public boolean canProvision(Label label) { return ((Boolean)Util.ifOverridden(() -> Boolean.valueOf(canProvision(new CloudState(label, 0))), Cloud.class, 
        
        getClass(), "canProvision", new Class[] { CloudState.class })).booleanValue(); }
  
  public boolean canProvision(CloudState state) { return canProvision(state.getLabel()); }
  
  public Descriptor<Cloud> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @Deprecated
  public static final DescriptorList<Cloud> ALL = new DescriptorList(Cloud.class);
  
  public static DescriptorExtensionList<Cloud, Descriptor<Cloud>> all() { return Jenkins.get().getDescriptorList(Cloud.class); }
  
  private static final PermissionScope PERMISSION_SCOPE = new PermissionScope(Cloud.class, new PermissionScope[0]);
  
  public static final Permission PROVISION = new Permission(Computer.PERMISSIONS, "Provision", 
      Messages._Cloud_ProvisionPermission_Description(), Jenkins.ADMINISTER, PERMISSION_SCOPE);
  
  @Initializer(before = InitMilestone.SYSTEM_CONFIG_LOADED)
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT"}, justification = "to guard against potential future compiler optimizations")
  public static void registerPermissions() { Objects.hash(new Object[] { PERMISSION_SCOPE, PROVISION }); }
  
  public String getIcon() { return "symbol-cloud"; }
  
  public String getIconClassName() { return "symbol-cloud"; }
  
  public String getIconAltText() { return getClass().getSimpleName().replace("Cloud", ""); }
  
  @RequirePOST
  public HttpResponse doDoDelete() throws IOException {
    checkPermission(Jenkins.ADMINISTER);
    (Jenkins.get()).clouds.remove(this);
    return new HttpRedirect("..");
  }
  
  @POST
  public HttpResponse doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(Jenkins.ADMINISTER);
    Jenkins j = Jenkins.get();
    Cloud cloud = j.getCloud(this.name);
    if (cloud == null)
      throw new ServletException("No such cloud " + this.name); 
    Cloud result = cloud.reconfigure(req, req.getSubmittedForm());
    String proposedName = result.name;
    if (!proposedName.equals(this.name) && j
      .getCloud(proposedName) != null)
      throw new Descriptor.FormException(Messages.CloudSet_CloudAlreadyExists(proposedName), "name"); 
    j.clouds.replace(this, result);
    j.save();
    return FormApply.success(".");
  }
  
  public Cloud reconfigure(@NonNull StaplerRequest req, JSONObject form) throws Descriptor.FormException {
    if (form == null)
      return null; 
    return (Cloud)getDescriptor().newInstance(req, form);
  }
}
