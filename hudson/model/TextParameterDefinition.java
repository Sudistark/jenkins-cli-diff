package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class TextParameterDefinition extends StringParameterDefinition {
  @DataBoundConstructor
  public TextParameterDefinition(@NonNull String name) { super(name); }
  
  public TextParameterDefinition(@NonNull String name, @CheckForNull String defaultValue, @CheckForNull String description) {
    this(name);
    setDefaultValue(defaultValue);
    setDescription(description);
  }
  
  public StringParameterValue getDefaultParameterValue() { return new TextParameterValue(getName(), getDefaultValue(), getDescription()); }
  
  public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
    TextParameterValue value = (TextParameterValue)req.bindJSON(TextParameterValue.class, jo);
    value.setDescription(getDescription());
    return value;
  }
  
  public ParameterValue createValue(String value) { return new TextParameterValue(getName(), value, getDescription()); }
  
  public int hashCode() {
    if (TextParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), getDefaultValue(), Boolean.valueOf(isTrim()) });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (TextParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    TextParameterDefinition other = (TextParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    if (!Objects.equals(getDefaultValue(), other.getDefaultValue()))
      return false; 
    return (isTrim() == other.isTrim());
  }
}
