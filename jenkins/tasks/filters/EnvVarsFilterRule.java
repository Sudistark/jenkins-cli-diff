package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.io.Serializable;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public interface EnvVarsFilterRule extends Serializable {
  void filter(@NonNull EnvVars paramEnvVars, @NonNull EnvVarsFilterRuleContext paramEnvVarsFilterRuleContext) throws EnvVarsFilterException;
  
  default String getDisplayName() {
    if (this instanceof Describable) {
      Descriptor<?> descriptor = ((Describable)this).getDescriptor();
      if (descriptor != null)
        return descriptor.getDisplayName(); 
    } 
    return getClass().getSimpleName();
  }
}
