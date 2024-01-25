package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.time.LocalDate;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Uptime extends Telemetry {
  private static final long START = System.nanoTime();
  
  @NonNull
  public String getDisplayName() { return "Uptime"; }
  
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2023, 10, 20); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2024, 1, 20); }
  
  public JSONObject createContent() { return (new JSONObject()).element("start", START).element("now", System.nanoTime()).element("components", buildComponentInformation()); }
}
