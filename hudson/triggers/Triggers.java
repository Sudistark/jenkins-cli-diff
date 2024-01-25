package hudson.triggers;

import hudson.model.Item;
import hudson.util.DescriptorList;
import java.util.List;

@Deprecated
public class Triggers {
  @Deprecated
  public static final List<TriggerDescriptor> TRIGGERS = new DescriptorList(Trigger.class);
  
  @Deprecated
  public static List<TriggerDescriptor> getApplicableTriggers(Item i) { return Trigger.for_(i); }
}
