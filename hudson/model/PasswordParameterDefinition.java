package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.Secret;
import java.util.Objects;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class PasswordParameterDefinition extends SimpleParameterDefinition {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String DEFAULT_VALUE = "<DEFAULT>";
  
  private Secret defaultValue;
  
  @Deprecated
  public PasswordParameterDefinition(@NonNull String name, @CheckForNull String defaultValue, @CheckForNull String description) {
    super(name, description);
    this.defaultValue = Secret.fromString(defaultValue);
  }
  
  @DataBoundConstructor
  public PasswordParameterDefinition(@NonNull String name, @CheckForNull Secret defaultValueAsSecret, @CheckForNull String description) {
    super(name, description);
    this.defaultValue = defaultValueAsSecret;
  }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
    if (defaultValue instanceof PasswordParameterValue) {
      PasswordParameterValue value = (PasswordParameterValue)defaultValue;
      return new PasswordParameterDefinition(getName(), Secret.toString(value.getValue()), getDescription());
    } 
    return this;
  }
  
  public ParameterValue createValue(String value) { return new PasswordParameterValue(getName(), value, getDescription()); }
  
  public PasswordParameterValue createValue(StaplerRequest req, JSONObject jo) {
    PasswordParameterValue value = (PasswordParameterValue)req.bindJSON(PasswordParameterValue.class, jo);
    if (value.getValue().getPlainText().equals("<DEFAULT>"))
      value = new PasswordParameterValue(getName(), getDefaultValue()); 
    value.setDescription(getDescription());
    return value;
  }
  
  public ParameterValue getDefaultParameterValue() { return new PasswordParameterValue(getName(), getDefaultValue(), getDescription()); }
  
  @NonNull
  public String getDefaultValue() { return Secret.toString(this.defaultValue); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Secret getDefaultValueAsSecret() { return this.defaultValue; }
  
  public void setDefaultValue(String defaultValue) { this.defaultValue = Secret.fromString(defaultValue); }
  
  public int hashCode() {
    if (PasswordParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), this.defaultValue });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (PasswordParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    PasswordParameterDefinition other = (PasswordParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    return Objects.equals(this.defaultValue, other.defaultValue);
  }
}
