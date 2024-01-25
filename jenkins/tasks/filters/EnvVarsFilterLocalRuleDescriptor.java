package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public abstract class EnvVarsFilterLocalRuleDescriptor extends Descriptor<EnvVarsFilterLocalRule> {
  public abstract boolean isApplicable(@NonNull Class<? extends EnvVarsFilterableBuilder> paramClass);
  
  public static List<EnvVarsFilterLocalRuleDescriptor> allApplicableFor(Class<? extends EnvVarsFilterableBuilder> builderClass) {
    DescriptorExtensionList<EnvVarsFilterLocalRule, EnvVarsFilterLocalRuleDescriptor> allSpecificRules = Jenkins.get().getDescriptorList(EnvVarsFilterLocalRule.class);
    return (List)allSpecificRules.stream()
      .filter(rule -> rule.isApplicable(builderClass))
      .collect(Collectors.toList());
  }
}
