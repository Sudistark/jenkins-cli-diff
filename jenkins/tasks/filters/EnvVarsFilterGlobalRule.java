package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import java.io.Serializable;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public interface EnvVarsFilterGlobalRule extends Describable<EnvVarsFilterGlobalRule>, EnvVarsFilterRule, ExtensionPoint, Serializable {
  default Descriptor<EnvVarsFilterGlobalRule> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  boolean isApplicable(@CheckForNull Run<?, ?> paramRun, @NonNull Object paramObject, @NonNull Launcher paramLauncher);
}
