package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UserLanguages extends Telemetry {
  private static final Map<String, AtomicLong> requestsByLanguage = new ConcurrentSkipListMap();
  
  private static Logger LOGGER = Logger.getLogger(UserLanguages.class.getName());
  
  @NonNull
  public String getId() { return UserLanguages.class.getName(); }
  
  @NonNull
  public String getDisplayName() { return "Browser languages"; }
  
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2018, 10, 1); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2019, 1, 1); }
  
  public JSONObject createContent() {
    if (requestsByLanguage.isEmpty())
      return null; 
    Map<String, AtomicLong> currentRequests = new TreeMap<String, AtomicLong>(requestsByLanguage);
    requestsByLanguage.clear();
    JSONObject payload = new JSONObject();
    for (Map.Entry<String, AtomicLong> entry : currentRequests.entrySet())
      payload.put((String)entry.getKey(), Long.valueOf(((AtomicLong)entry.getValue()).longValue())); 
    return payload;
  }
}
