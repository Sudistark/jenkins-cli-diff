package hudson.triggers;

import hudson.model.Descriptor;
import hudson.model.Item;

public abstract class TriggerDescriptor extends Descriptor<Trigger<?>> {
  protected TriggerDescriptor(Class<? extends Trigger<?>> clazz) { super(clazz); }
  
  protected TriggerDescriptor() {}
  
  public abstract boolean isApplicable(Item paramItem);
}
