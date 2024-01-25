package hudson.util;

public interface VariableResolver<V> {
  public static final VariableResolver NONE = name -> null;
  
  V resolve(String paramString);
}
