package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.VersionNumber;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import jenkins.model.Jenkins;
import jenkins.telemetry.Telemetry;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class SecuritySystemProperties extends Telemetry {
  @NonNull
  public String getId() { return "security-system-properties"; }
  
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2018, 9, 1); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2018, 12, 1); }
  
  @NonNull
  public String getDisplayName() { return "Use of Security-related Java system properties"; }
  
  @NonNull
  public JSONObject createContent() {
    Map<String, String> security = new TreeMap<String, String>();
    putBoolean(security, "hudson.ConsoleNote.INSECURE", false);
    putBoolean(security, "hudson.logging.LogRecorderManager.skipPermissionCheck", false);
    putBoolean(security, "hudson.model.AbstractItem.skipPermissionCheck", false);
    putBoolean(security, "hudson.model.ParametersAction.keepUndefinedParameters", false);
    putBoolean(security, "hudson.model.Run.skipPermissionCheck", false);
    putBoolean(security, "hudson.model.UpdateCenter.skipPermissionCheck", false);
    putBoolean(security, "hudson.model.User.allowNonExistentUserToLogin", false);
    putBoolean(security, "hudson.model.User.allowUserCreationViaUrl", false);
    putBoolean(security, "hudson.model.User.SECURITY_243_FULL_DEFENSE", true);
    putBoolean(security, "hudson.model.User.skipPermissionCheck", false);
    putBoolean(security, "hudson.PluginManager.skipPermissionCheck", false);
    putBoolean(security, "hudson.remoting.URLDeserializationHelper.avoidUrlWrapping", false);
    putBoolean(security, "hudson.search.Search.skipPermissionCheck", false);
    putBoolean(security, "jenkins.security.ClassFilterImpl.SUPPRESS_WHITELIST", false);
    putBoolean(security, "jenkins.security.ClassFilterImpl.SUPPRESS_ALL", false);
    putBoolean(security, "org.kohsuke.stapler.Facet.allowViewNamePathTraversal", false);
    putBoolean(security, "org.kohsuke.stapler.jelly.CustomJellyContext.escapeByDefault", true);
    putStringInfo(security, "hudson.model.ParametersAction.safeParameters");
    putStringInfo(security, "hudson.model.DirectoryBrowserSupport.CSP");
    putStringInfo(security, "hudson.security.HudsonPrivateSecurityRealm.ID_REGEX");
    Map<String, Object> info = new TreeMap<String, Object>();
    VersionNumber jenkinsVersion = Jenkins.getVersion();
    info.put("core", (jenkinsVersion != null) ? jenkinsVersion.toString() : "UNKNOWN");
    info.put("clientDate", clientDateString());
    info.put("properties", security);
    return JSONObject.fromObject(info);
  }
  
  private static String clientDateString() {
    tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    df.setTimeZone(tz);
    return df.format(new Date());
  }
  
  private static void putBoolean(Map<String, String> propertiesMap, String systemProperty, boolean defaultValue) { propertiesMap.put(systemProperty, Boolean.toString(SystemProperties.getBoolean(systemProperty, defaultValue))); }
  
  private static void putStringInfo(Map<String, String> propertiesMap, String systemProperty) {
    String reportedValue = "null";
    String value = SystemProperties.getString(systemProperty);
    if (value != null)
      reportedValue = Integer.toString(value.length()); 
    propertiesMap.put(systemProperty, reportedValue);
  }
}
