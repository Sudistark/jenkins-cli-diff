package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;
import hudson.model.View;
import java.util.List;
import jenkins.model.Jenkins;

public abstract class ViewJobFilter extends Object implements ExtensionPoint, Describable<ViewJobFilter> {
  public static DescriptorExtensionList<ViewJobFilter, Descriptor<ViewJobFilter>> all() { return Jenkins.get().getDescriptorList(ViewJobFilter.class); }
  
  public Descriptor<ViewJobFilter> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public abstract List<TopLevelItem> filter(List<TopLevelItem> paramList1, List<TopLevelItem> paramList2, View paramView);
}
