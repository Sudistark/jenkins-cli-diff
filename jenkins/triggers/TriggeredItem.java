package jenkins.triggers;

import hudson.model.Item;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import java.util.Map;

public interface TriggeredItem extends Item {
  Map<TriggerDescriptor, Trigger<?>> getTriggers();
}
