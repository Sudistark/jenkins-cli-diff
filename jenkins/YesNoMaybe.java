package jenkins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public static enum YesNoMaybe {
  YES, NO, MAYBE;
  
  @SuppressFBWarnings(value = {"NP_BOOLEAN_RETURN_NULL"}, justification = "bridge method for backward compatibility")
  public static Boolean toBoolean(YesNoMaybe v) {
    if (v == null)
      return null; 
    return v.toBool();
  }
  
  @SuppressFBWarnings(value = {"NP_BOOLEAN_RETURN_NULL"}, justification = "bridge method for backward compatibility")
  public Boolean toBool() {
    switch (null.$SwitchMap$jenkins$YesNoMaybe[ordinal()]) {
      case 1:
        return Boolean.valueOf(true);
      case 2:
        return Boolean.valueOf(false);
    } 
    return null;
  }
}
