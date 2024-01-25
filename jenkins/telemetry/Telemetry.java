package jenkins.telemetry;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.PluginWrapper;
import hudson.model.UsageStatistics;
import hudson.util.VersionNumber;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

public abstract class Telemetry implements ExtensionPoint {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  static String ENDPOINT = SystemProperties.getString(Telemetry.class.getName() + ".endpoint", "https://uplink.jenkins.io/events");
  
  private static final Logger LOGGER = Logger.getLogger(Telemetry.class.getName());
  
  @NonNull
  public String getId() { return getClass().getName(); }
  
  @NonNull
  public abstract String getDisplayName();
  
  @NonNull
  public abstract LocalDate getStart();
  
  @NonNull
  public abstract LocalDate getEnd();
  
  @CheckForNull
  public abstract JSONObject createContent();
  
  public static ExtensionList<Telemetry> all() { return ExtensionList.lookup(Telemetry.class); }
  
  public static boolean isDisabled() {
    if (UsageStatistics.DISABLED)
      return true; 
    jenkins = Jenkins.getInstanceOrNull();
    return (jenkins == null || !jenkins.isUsageStatisticsCollected());
  }
  
  public boolean isActivePeriod() {
    LocalDate now = LocalDate.now();
    return (now.isAfter(getStart()) && now.isBefore(getEnd()));
  }
  
  protected final Map<String, String> buildComponentInformation() {
    Map<String, String> components = new TreeMap<String, String>();
    VersionNumber core = Jenkins.getVersion();
    components.put("jenkins-core", (core == null) ? "" : core.toString());
    for (PluginWrapper plugin : (Jenkins.get()).pluginManager.getPlugins()) {
      if (plugin.isActive())
        components.put(plugin.getShortName(), plugin.getVersion()); 
    } 
    return components;
  }
}
