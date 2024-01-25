package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.util.Objects;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

public class StringParameterDefinition extends SimpleParameterDefinition {
  private String defaultValue;
  
  private boolean trim;
  
  @DataBoundConstructor
  public StringParameterDefinition(@NonNull String name) { super(name); }
  
  public StringParameterDefinition(@NonNull String name, @CheckForNull String defaultValue, @CheckForNull String description, boolean trim) {
    this(name);
    setDefaultValue(defaultValue);
    setDescription(description);
    setTrim(trim);
  }
  
  public StringParameterDefinition(@NonNull String name, @CheckForNull String defaultValue, @CheckForNull String description) {
    this(name);
    setDefaultValue(defaultValue);
    setDescription(description);
  }
  
  public StringParameterDefinition(@NonNull String name, @CheckForNull String defaultValue) {
    this(name);
    setDefaultValue(defaultValue);
  }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
    if (defaultValue instanceof StringParameterValue) {
      StringParameterValue value = (StringParameterValue)defaultValue;
      return new StringParameterDefinition(getName(), value.value, getDescription());
    } 
    return this;
  }
  
  @NonNull
  public String getDefaultValue() { return this.defaultValue; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public String getDefaultValue4Build() {
    if (isTrim())
      return Util.fixNull(this.defaultValue).trim(); 
    return this.defaultValue;
  }
  
  @DataBoundSetter
  public void setDefaultValue(@CheckForNull String defaultValue) { this.defaultValue = Util.fixEmpty(defaultValue); }
  
  public boolean isTrim() { return this.trim; }
  
  @DataBoundSetter
  public void setTrim(boolean trim) { this.trim = trim; }
  
  public StringParameterValue getDefaultParameterValue() {
    StringParameterValue value = new StringParameterValue(getName(), this.defaultValue, getDescription());
    if (isTrim())
      value.doTrim(); 
    return value;
  }
  
  public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
    StringParameterValue value = (StringParameterValue)req.bindJSON(StringParameterValue.class, jo);
    if (isTrim())
      value.doTrim(); 
    value.setDescription(getDescription());
    return value;
  }
  
  public ParameterValue createValue(String str) {
    StringParameterValue value = new StringParameterValue(getName(), str, getDescription());
    if (isTrim())
      value.doTrim(); 
    return value;
  }
  
  public int hashCode() {
    if (StringParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), this.defaultValue, Boolean.valueOf(this.trim) });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (StringParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    StringParameterDefinition other = (StringParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    if (!Objects.equals(this.defaultValue, other.defaultValue))
      return false; 
    return (this.trim == other.trim);
  }
}
