package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.util.Secret;
import hudson.util.VariableResolver;
import java.util.Locale;
import org.kohsuke.stapler.DataBoundConstructor;

public class PasswordParameterValue extends ParameterValue {
  @NonNull
  private final Secret value;
  
  public PasswordParameterValue(String name, String value) { this(name, value, null); }
  
  @Deprecated
  public PasswordParameterValue(String name, String value, String description) {
    super(name, description);
    this.value = Secret.fromString(value);
  }
  
  @DataBoundConstructor
  public PasswordParameterValue(String name, Secret value, String description) {
    super(name, description);
    this.value = value;
  }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    String v = Secret.toString(this.value);
    env.put(this.name, v);
    env.put(this.name.toUpperCase(Locale.ENGLISH), v);
  }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) { return name -> this.name.equals(name) ? Secret.toString(this.value) : null; }
  
  public boolean isSensitive() { return true; }
  
  @NonNull
  public Secret getValue() { return this.value; }
  
  public String getShortDescription() {
    return this.name + "=****";
  }
}
