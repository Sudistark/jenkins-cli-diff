package jenkins.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification = "Currently Jenkins instance may have one ond only one context")
public class SystemProperties {
  private static final Handler NULL_HANDLER = key -> null;
  
  @SuppressFBWarnings(value = {"NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"}, justification = "the field is initialized by a static initializer, not a constructor")
  @NonNull
  private static Handler handler = NULL_HANDLER;
  
  private static final Set<String> ALLOW_ON_AGENT = Collections.synchronizedSet(new HashSet());
  
  public static void allowOnAgent(String key) { ALLOW_ON_AGENT.add(key); }
  
  private static final Logger LOGGER = Logger.getLogger(SystemProperties.class.getName());
  
  @CheckForNull
  public static String getString(String key) { return getString(key, null); }
  
  public static String getString(String key, @CheckForNull String def) { return getString(key, def, Level.CONFIG); }
  
  public static String getString(String key, @CheckForNull String def, Level logLevel) {
    String value = System.getProperty(key);
    if (value != null) {
      if (LOGGER.isLoggable(logLevel))
        LOGGER.log(logLevel, "Property (system): {0} => {1}", new Object[] { key, value }); 
      return value;
    } 
    value = handler.getString(key);
    if (value != null) {
      if (LOGGER.isLoggable(logLevel))
        LOGGER.log(logLevel, "Property (context): {0} => {1}", new Object[] { key, value }); 
      return value;
    } 
    value = def;
    if (LOGGER.isLoggable(logLevel))
      LOGGER.log(logLevel, "Property (default): {0} => {1}", new Object[] { key, value }); 
    return value;
  }
  
  public static boolean getBoolean(String name) { return getBoolean(name, false); }
  
  public static boolean getBoolean(String name, boolean def) {
    String v = getString(name);
    if (v != null)
      return Boolean.parseBoolean(v); 
    return def;
  }
  
  @CheckForNull
  public static Boolean optBoolean(String name) {
    String v = getString(name);
    return (v == null) ? null : Boolean.valueOf(Boolean.parseBoolean(v));
  }
  
  @CheckForNull
  public static Integer getInteger(String name) { return getInteger(name, null); }
  
  public static Integer getInteger(String name, Integer def) { return getInteger(name, def, Level.CONFIG); }
  
  public static Integer getInteger(String name, Integer def, Level logLevel) {
    String v = getString(name);
    if (v != null)
      try {
        return Integer.decode(v);
      } catch (NumberFormatException e) {
        if (LOGGER.isLoggable(logLevel))
          LOGGER.log(logLevel, "Property. Value is not integer: {0} => {1}", new Object[] { name, v }); 
      }  
    return def;
  }
  
  @CheckForNull
  public static Long getLong(String name) { return getLong(name, null); }
  
  public static Long getLong(String name, Long def) { return getLong(name, def, Level.CONFIG); }
  
  public static Long getLong(String name, Long def, Level logLevel) {
    String v = getString(name);
    if (v != null)
      try {
        return Long.decode(v);
      } catch (NumberFormatException e) {
        if (LOGGER.isLoggable(logLevel))
          LOGGER.log(logLevel, "Property. Value is not long: {0} => {1}", new Object[] { name, v }); 
      }  
    return def;
  }
}
