package hudson.views;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.View;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

public abstract class MyViewsTabBar extends AbstractDescribableImpl<MyViewsTabBar> implements ExtensionPoint {
  public static DescriptorExtensionList<MyViewsTabBar, Descriptor<MyViewsTabBar>> all() { return Jenkins.get().getDescriptorList(MyViewsTabBar.class); }
  
  public MyViewsTabBarDescriptor getDescriptor() { return (MyViewsTabBarDescriptor)super.getDescriptor(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public List<View> sort(@NonNull List<? extends View> views) {
    List<View> result = new ArrayList<View>(views);
    result.sort(Comparator.comparing(View::getDisplayName));
    return result;
  }
}
