package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public class EnvVarsFilterException extends AbortException {
  private EnvVarsFilterRule rule;
  
  private String variableName;
  
  public EnvVarsFilterException(String message) { super(message); }
  
  @NonNull
  public EnvVarsFilterException withRule(@NonNull EnvVarsFilterRule rule) {
    this.rule = rule;
    return this;
  }
  
  @NonNull
  public EnvVarsFilterException withVariable(@NonNull String variableName) {
    this.variableName = variableName;
    return this;
  }
  
  @CheckForNull
  public EnvVarsFilterRule getRule() { return this.rule; }
  
  @NonNull
  public String getMessage() {
    String message = super.getMessage();
    if (this.variableName != null)
      message = message + " due to variable '" + message + "'"; 
    if (this.rule != null)
      if (this.rule instanceof EnvVarsFilterGlobalRule) {
        message = message + " detected by the global rule " + message;
      } else {
        message = message + " detected by " + message;
      }  
    return message;
  }
}
