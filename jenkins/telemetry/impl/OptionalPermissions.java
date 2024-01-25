package jenkins.telemetry.impl;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.Run;
import hudson.security.Permission;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import jenkins.model.Jenkins;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class OptionalPermissions extends Telemetry {
  private static final Set<String> OPTIONAL_PERMISSION_IDS = Set.of(Computer.EXTENDED_READ
      
      .getId(), Item.EXTENDED_READ
      .getId(), Item.WIPEOUT
      .getId(), Jenkins.MANAGE
      .getId(), Jenkins.SYSTEM_READ
      .getId(), Run.ARTIFACTS
      .getId(), "com.cloudbees.plugins.credentials.CredentialsProvider.UseOwn", "com.cloudbees.plugins.credentials.CredentialsProvider.UseItem");
  
  public String getDisplayName() { return "Activation of permissions that are not enabled by default"; }
  
  public LocalDate getStart() { return LocalDate.of(2022, 11, 1); }
  
  public LocalDate getEnd() { return LocalDate.of(2023, 3, 1); }
  
  public JSONObject createContent() {
    Map<String, Boolean> permissions = new TreeMap<String, Boolean>();
    for (Permission p : Permission.getAll()) {
      if (OPTIONAL_PERMISSION_IDS.contains(p.getId()))
        permissions.put(p.getId(), Boolean.valueOf(p.getEnabled())); 
    } 
    JSONObject payload = new JSONObject();
    payload.put("components", buildComponentInformation());
    payload.put("permissions", permissions);
    return payload;
  }
}
