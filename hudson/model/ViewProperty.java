package hudson.model;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class ViewProperty extends Object implements ReconfigurableDescribable<ViewProperty>, ExtensionPoint {
  protected View view;
  
  final void setView(View view) { this.view = view; }
  
  public ViewPropertyDescriptor getDescriptor() { return (ViewPropertyDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<ViewProperty, ViewPropertyDescriptor> all() { return Jenkins.get().getDescriptorList(ViewProperty.class); }
  
  public ViewProperty reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException { return (form == null) ? null : (ViewProperty)getDescriptor().newInstance(req, form); }
}
