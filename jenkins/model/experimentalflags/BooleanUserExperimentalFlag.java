package jenkins.model.experimentalflags;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class BooleanUserExperimentalFlag extends UserExperimentalFlag<Boolean> {
  protected BooleanUserExperimentalFlag(@NonNull String flagKey) { super(flagKey); }
  
  @NonNull
  public Boolean getDefaultValue() { return Boolean.valueOf(false); }
  
  public Object serializeValue(Boolean rawValue) {
    if (rawValue == null)
      return null; 
    return rawValue.booleanValue() ? "true" : "false";
  }
  
  protected Boolean deserializeValue(Object serializedValue) {
    if (serializedValue.equals("true"))
      return Boolean.TRUE; 
    if (serializedValue.equals("false"))
      return Boolean.FALSE; 
    return null;
  }
}
