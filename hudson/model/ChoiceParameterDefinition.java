package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class ChoiceParameterDefinition extends SimpleParameterDefinition {
  public static final String CHOICES_DELIMITER = "\\r?\\n";
  
  @Deprecated
  public static final String CHOICES_DELIMETER = "\\r?\\n";
  
  private List<String> choices;
  
  private final String defaultValue;
  
  public static boolean areValidChoices(@NonNull String choices) {
    String strippedChoices = choices.trim();
    return (strippedChoices != null && !strippedChoices.isEmpty() && strippedChoices.split("\\r?\\n").length > 0);
  }
  
  public ChoiceParameterDefinition(@NonNull String name, @NonNull String choices, @CheckForNull String description) {
    super(name, description);
    setChoicesText(choices);
    this.defaultValue = null;
  }
  
  public ChoiceParameterDefinition(@NonNull String name, @NonNull String[] choices, @CheckForNull String description) {
    super(name, description);
    this.choices = (List)Stream.of(choices).map(Util::fixNull).collect(Collectors.toCollection(ArrayList::new));
    this.defaultValue = null;
  }
  
  private ChoiceParameterDefinition(@NonNull String name, @NonNull List<String> choices, String defaultValue, @CheckForNull String description) {
    super(name, description);
    this.choices = Util.fixNull(choices);
    this.defaultValue = defaultValue;
  }
  
  @DataBoundConstructor
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public ChoiceParameterDefinition(String name, String description) {
    super(name, description);
    this.choices = new ArrayList();
    this.defaultValue = null;
  }
  
  @DataBoundSetter
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void setChoices(Object choices) {
    if (choices instanceof String) {
      setChoicesText((String)choices);
      return;
    } 
    if (choices instanceof List) {
      ArrayList<String> newChoices = new ArrayList<String>();
      for (Object o : (List)choices) {
        if (o != null)
          newChoices.add(o.toString()); 
      } 
      this.choices = newChoices;
      return;
    } 
    throw new IllegalArgumentException("expected String or List, but got " + choices.getClass().getName());
  }
  
  private void setChoicesText(@NonNull String choices) { this.choices = Arrays.asList(choices.split("\\r?\\n")); }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
    if (defaultValue instanceof StringParameterValue) {
      StringParameterValue value = (StringParameterValue)defaultValue;
      return new ChoiceParameterDefinition(getName(), this.choices, value.value, getDescription());
    } 
    return this;
  }
  
  @Exported
  @NonNull
  public List<String> getChoices() { return this.choices; }
  
  public String getChoicesText() { return String.join("\n", this.choices); }
  
  @CheckForNull
  public StringParameterValue getDefaultParameterValue() {
    if (this.defaultValue == null) {
      if (this.choices.isEmpty())
        return null; 
      return new StringParameterValue(getName(), (String)this.choices.get(0), getDescription());
    } 
    return new StringParameterValue(getName(), this.defaultValue, getDescription());
  }
  
  public boolean isValid(ParameterValue value) { return this.choices.contains(((StringParameterValue)value).getValue()); }
  
  public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
    StringParameterValue value = (StringParameterValue)req.bindJSON(StringParameterValue.class, jo);
    value.setDescription(getDescription());
    checkValue(value, value.getValue());
    return value;
  }
  
  private void checkValue(StringParameterValue value, String value2) {
    if (!isValid(value))
      throw new IllegalArgumentException("Illegal choice for parameter " + getName() + ": " + value2); 
  }
  
  public StringParameterValue createValue(String value) {
    StringParameterValue parameterValue = new StringParameterValue(getName(), value, getDescription());
    checkValue(parameterValue, value);
    return parameterValue;
  }
  
  public int hashCode() {
    if (ChoiceParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), this.choices, this.defaultValue });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (ChoiceParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    ChoiceParameterDefinition other = (ChoiceParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    if (!Objects.equals(this.choices, other.choices))
      return false; 
    return Objects.equals(this.defaultValue, other.defaultValue);
  }
}
