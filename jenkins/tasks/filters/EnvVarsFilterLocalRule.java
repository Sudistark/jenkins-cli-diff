package jenkins.tasks.filters;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.io.Serializable;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public interface EnvVarsFilterLocalRule extends Describable<EnvVarsFilterLocalRule>, EnvVarsFilterRule, ExtensionPoint, Serializable {
  default EnvVarsFilterLocalRuleDescriptor getDescriptor() { return (EnvVarsFilterLocalRuleDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
}
