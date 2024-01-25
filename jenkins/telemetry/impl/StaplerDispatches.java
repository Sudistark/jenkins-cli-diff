package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class StaplerDispatches extends Telemetry {
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2018, 10, 10); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2019, 8, 1); }
  
  @NonNull
  public String getDisplayName() { return "Stapler request handling"; }
  
  public JSONObject createContent() {
    if (traces.isEmpty())
      return null; 
    Map<String, Object> info = new TreeMap<String, Object>();
    info.put("components", buildComponentInformation());
    info.put("dispatches", buildDispatches());
    return JSONObject.fromObject(info);
  }
  
  private Object buildDispatches() {
    Set<String> currentTraces = new TreeSet<String>(traces);
    traces.clear();
    return currentTraces;
  }
  
  private static final Set<String> traces = new ConcurrentSkipListSet();
}
