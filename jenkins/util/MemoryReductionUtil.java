package jenkins.util;

import hudson.Util;
import java.util.HashMap;
import java.util.Map;

public class MemoryReductionUtil {
  public static int preallocatedHashmapCapacity(int elementsToHold) {
    if (elementsToHold <= 0)
      return 0; 
    if (elementsToHold < 3)
      return elementsToHold + 1; 
    return elementsToHold + elementsToHold / 3;
  }
  
  public static Map getPresizedMutableMap(int elementCount) { return new HashMap(preallocatedHashmapCapacity(elementCount)); }
  
  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  
  public static String[] internInPlace(String[] input) {
    if (input == null)
      return null; 
    if (input.length == 0)
      return EMPTY_STRING_ARRAY; 
    for (int i = 0; i < input.length; i++)
      input[i] = Util.intern(input[i]); 
    return input;
  }
}
