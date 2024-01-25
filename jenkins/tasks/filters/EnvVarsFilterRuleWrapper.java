package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class EnvVarsFilterRuleWrapper implements Serializable {
  private static final long serialVersionUID = -8647970104978388598L;
  
  private List<EnvVarsFilterRule> rules;
  
  public EnvVarsFilterRuleWrapper(@NonNull List<EnvVarsFilterRule> rules) { this.rules = rules; }
  
  @NonNull
  public static EnvVarsFilterRuleWrapper createRuleWrapper(@CheckForNull Run<?, ?> run, @NonNull Object builder, @NonNull Launcher launcher, @NonNull List<EnvVarsFilterLocalRule> localRules) {
    List<EnvVarsFilterGlobalRule> globalRules = EnvVarsFilterGlobalConfiguration.getAllActivatedGlobalRules();
    List<EnvVarsFilterGlobalRule> applicableGlobalRules = (List)globalRules.stream().filter(rule -> rule.isApplicable(run, builder, launcher)).collect(Collectors.toList());
    List<EnvVarsFilterRule> applicableRules = new ArrayList<EnvVarsFilterRule>();
    applicableRules.addAll(localRules);
    applicableRules.addAll(applicableGlobalRules);
    return new EnvVarsFilterRuleWrapper(applicableRules);
  }
  
  public void filter(@NonNull EnvVars envVars, @NonNull Launcher launcher, @NonNull TaskListener listener) throws EnvVarsFilterException {
    EnvVarsFilterRuleContext context = new EnvVarsFilterRuleContext(launcher, listener);
    for (EnvVarsFilterRule rule : this.rules) {
      try {
        rule.filter(envVars, context);
      } catch (EnvVarsFilterException e) {
        String message = String.format("Environment variable filtering failed due to violation with the message: %s", new Object[] { e.getMessage() });
        context.getTaskListener().error(message);
        throw e;
      } 
    } 
  }
}
