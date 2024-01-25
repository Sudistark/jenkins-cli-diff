package hudson.model;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class UserProperty extends Object implements ReconfigurableDescribable<UserProperty>, ExtensionPoint {
  protected User user;
  
  protected void setUser(User u) { this.user = u; }
  
  public UserPropertyDescriptor getDescriptor() { return (UserPropertyDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<UserProperty, UserPropertyDescriptor> all() { return Jenkins.get().getDescriptorList(UserProperty.class); }
  
  public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException { return (form == null) ? null : (UserProperty)getDescriptor().newInstance(req, form); }
}
