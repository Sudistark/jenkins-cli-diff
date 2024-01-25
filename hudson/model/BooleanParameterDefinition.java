package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

public class BooleanParameterDefinition extends SimpleParameterDefinition {
  private boolean defaultValue;
  
  @DataBoundConstructor
  public BooleanParameterDefinition(@NonNull String name) { super(name); }
  
  public BooleanParameterDefinition(@NonNull String name, boolean defaultValue, @CheckForNull String description) {
    this(name);
    setDefaultValue(defaultValue);
    setDescription(description);
  }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
    if (defaultValue instanceof BooleanParameterValue) {
      BooleanParameterValue value = (BooleanParameterValue)defaultValue;
      return new BooleanParameterDefinition(getName(), value.value, getDescription());
    } 
    return this;
  }
  
  public boolean isDefaultValue() { return this.defaultValue; }
  
  @DataBoundSetter
  public void setDefaultValue(boolean defaultValue) { this.defaultValue = defaultValue; }
  
  public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
    BooleanParameterValue value = (BooleanParameterValue)req.bindJSON(BooleanParameterValue.class, jo);
    value.setDescription(getDescription());
    return value;
  }
  
  public ParameterValue createValue(String value) { return new BooleanParameterValue(getName(), Boolean.parseBoolean(value), getDescription()); }
  
  public BooleanParameterValue getDefaultParameterValue() { return new BooleanParameterValue(getName(), this.defaultValue, getDescription()); }
  
  public int hashCode() {
    if (BooleanParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), Boolean.valueOf(this.defaultValue) });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (BooleanParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    BooleanParameterDefinition other = (BooleanParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    return (this.defaultValue == other.defaultValue);
  }
}
