package hudson.triggers;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.AdministrativeMonitor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class SlowTriggerAdminMonitor extends AdministrativeMonitor {
  @NonNull
  private final Map<String, Value> errors = new ConcurrentHashMap();
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static int MAX_ENTRIES = SystemProperties.getInteger(SlowTriggerAdminMonitor.class.getName() + ".maxEntries", Integer.valueOf(10)).intValue();
  
  @NonNull
  private static final Logger LOGGER = Logger.getLogger(SlowTriggerAdminMonitor.class.getName());
  
  @NonNull
  public static SlowTriggerAdminMonitor getInstance() { return (SlowTriggerAdminMonitor)ExtensionList.lookup(SlowTriggerAdminMonitor.class).get(0); }
  
  public boolean isActivated() { return !this.errors.isEmpty(); }
  
  @NonNull
  public String getDisplayName() { return Messages.SlowTriggerAdminMonitor_DisplayName(); }
  
  public void clear() {
    synchronized (this.errors) {
      this.errors.clear();
    } 
  }
  
  public void report(@NonNull Class<? extends TriggerDescriptor> trigger, @NonNull String fullJobName, long duration) {
    synchronized (this.errors) {
      if (this.errors.size() >= MAX_ENTRIES && !this.errors.containsKey(trigger.getName())) {
        String oldest_trigger = null;
        LocalDateTime oldest_time = null;
        for (Map.Entry<String, Value> entry : this.errors.entrySet()) {
          String local_trigger = (String)entry.getKey();
          if (oldest_trigger == null || ((Value)entry
            .getValue()).time.compareTo(oldest_time) < 0) {
            oldest_trigger = local_trigger;
            oldest_time = ((Value)entry.getValue()).time;
          } 
        } 
        this.errors.remove(oldest_trigger);
      } 
    } 
    this.errors.put(trigger.getName(), new Value(trigger, fullJobName, duration));
  }
  
  @NonNull
  public Map<String, Value> getErrors() { return new HashMap(this.errors); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RequirePOST
  @NonNull
  public HttpResponse doClear() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    clear();
    return HttpResponses.redirectViaContextPath("/manage");
  }
}
