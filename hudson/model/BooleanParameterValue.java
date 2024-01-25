package hudson.model;

import hudson.EnvVars;
import hudson.util.VariableResolver;
import java.util.Locale;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class BooleanParameterValue extends ParameterValue {
  @Exported(visibility = 4)
  public final boolean value;
  
  @DataBoundConstructor
  public BooleanParameterValue(String name, boolean value) { this(name, value, null); }
  
  public BooleanParameterValue(String name, boolean value, String description) {
    super(name, description);
    this.value = value;
  }
  
  public Boolean getValue() { return Boolean.valueOf(this.value); }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    env.put(this.name, Boolean.toString(this.value));
    env.put(this.name.toUpperCase(Locale.ENGLISH), Boolean.toString(this.value));
  }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) { return name -> this.name.equals(name) ? Boolean.toString(this.value) : null; }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    if (!super.equals(o))
      return false; 
    BooleanParameterValue that = (BooleanParameterValue)o;
    return (this.value == that.value);
  }
  
  public int hashCode() {
    result = super.hashCode();
    return 31 * result + (this.value ? 1 : 0);
  }
  
  public String toString() { return "(BooleanParameterValue) " + getName() + "='" + this.value + "'"; }
  
  public String getShortDescription() { return this.name + "=" + this.name; }
}
