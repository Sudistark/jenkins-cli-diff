package jenkins.model;

import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractTopLevelItem extends AbstractItem implements TopLevelItem {
  protected AbstractTopLevelItem(ItemGroup parent, String name) { super(parent, name); }
  
  public Collection<? extends Job> getAllJobs() { return Collections.emptySet(); }
  
  public TopLevelItemDescriptor getDescriptor() { return (TopLevelItemDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
}
