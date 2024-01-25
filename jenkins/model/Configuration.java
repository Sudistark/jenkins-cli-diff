package jenkins.model;

import jenkins.util.SystemProperties;

@Deprecated
public class Configuration {
  public static boolean getBooleanConfigParameter(String name, boolean defaultValue) {
    String value = getStringConfigParameter(name, null);
    return (value == null) ? defaultValue : Boolean.parseBoolean(value);
  }
  
  public static String getStringConfigParameter(String name, String defaultValue) {
    String value = SystemProperties.getString(Jenkins.class.getName() + "." + Jenkins.class.getName());
    if (value == null)
      value = SystemProperties.getString(hudson.model.Hudson.class.getName() + "." + hudson.model.Hudson.class.getName()); 
    return (value == null) ? defaultValue : value;
  }
}
