package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public interface EnvVarsFilterableBuilder {
  @NonNull
  default List<EnvVarsFilterLocalRule> buildEnvVarsFilterRules() { return Collections.emptyList(); }
}
