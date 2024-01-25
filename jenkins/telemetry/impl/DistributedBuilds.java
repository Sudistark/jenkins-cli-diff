package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Node;
import hudson.slaves.Cloud;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import jenkins.diagnostics.ControllerExecutorsAgents;
import jenkins.diagnostics.ControllerExecutorsNoAgents;
import jenkins.model.Jenkins;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class DistributedBuilds extends Telemetry {
  @NonNull
  public String getDisplayName() { return "Distributed Builds"; }
  
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2022, 12, 1); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2023, 3, 1); }
  
  public JSONObject createContent() {
    JSONObject payload = new JSONObject();
    payload.put("controllerExecutors", Integer.valueOf(Jenkins.get().getNumExecutors()));
    payload.put("controllerExecutorsWithAgentsWarning", Boolean.valueOf(((ControllerExecutorsAgents)ExtensionList.lookupSingleton(ControllerExecutorsAgents.class)).isEnabled()));
    payload.put("controllerExecutorsWithoutAgentsWarning", Boolean.valueOf(((ControllerExecutorsNoAgents)ExtensionList.lookupSingleton(ControllerExecutorsNoAgents.class)).isEnabled()));
    Map<String, Integer> clouds = new HashMap<String, Integer>();
    for (Cloud cloud : (Jenkins.get()).clouds)
      clouds.compute(cloud.getClass().getName(), (key, value) -> Integer.valueOf((value == null) ? 1 : (value.intValue() + 1))); 
    payload.put("clouds", clouds);
    Map<String, Integer> agents = new HashMap<String, Integer>();
    for (Node agent : Jenkins.get().getNodes())
      agents.compute(agent.getClass().getName(), (key, value) -> Integer.valueOf((value == null) ? 1 : (value.intValue() + 1))); 
    payload.put("agents", agents);
    payload.put("items", Integer.valueOf(Jenkins.get().getAllItems().size()));
    payload.put("components", buildComponentInformation());
    return payload;
  }
}
