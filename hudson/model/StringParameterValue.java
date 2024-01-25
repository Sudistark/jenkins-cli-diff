package hudson.model;

import hudson.EnvVars;
import hudson.Util;
import hudson.util.VariableResolver;
import java.util.Locale;
import java.util.Objects;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class StringParameterValue extends ParameterValue {
  @Exported(visibility = 4)
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public String value;
  
  @DataBoundConstructor
  public StringParameterValue(String name, String value) { this(name, value, null); }
  
  public StringParameterValue(String name, String value, String description) {
    super(name, description);
    this.value = Util.fixNull(value);
  }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    env.put(this.name, this.value);
    env.put(this.name.toUpperCase(Locale.ENGLISH), this.value);
  }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) { return name -> this.name.equals(name) ? this.value : null; }
  
  public String getValue() { return this.value; }
  
  public void doTrim() {
    if (this.value != null)
      this.value = this.value.trim(); 
  }
  
  public int hashCode() {
    int prime = 31;
    result = super.hashCode();
    return 31 * result + ((this.value == null) ? 0 : this.value.hashCode());
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!super.equals(obj))
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    StringParameterValue other = (StringParameterValue)obj;
    if (!Objects.equals(this.value, other.value))
      return false; 
    return true;
  }
  
  public String toString() { return "(StringParameterValue) " + getName() + "='" + this.value + "'"; }
  
  public String getShortDescription() { return this.name + "=" + this.name; }
}
