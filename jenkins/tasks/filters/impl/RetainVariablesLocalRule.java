package jenkins.tasks.filters.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jenkins.tasks.filters.EnvVarsFilterLocalRule;
import jenkins.tasks.filters.EnvVarsFilterRuleContext;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class RetainVariablesLocalRule implements EnvVarsFilterLocalRule {
  private static final List<String> CHARACTERISTIC_ENV_VARS = Arrays.asList(new String[] { "jenkins_server_cookie", "hudson_server_cookie", "job_name", "job_base_name", "build_number", "build_id", "build_tag" });
  
  private String variables = "";
  
  private boolean retainCharacteristicEnvVars = true;
  
  private ProcessVariablesHandling processVariablesHandling = ProcessVariablesHandling.RESET;
  
  @DataBoundSetter
  public void setVariables(@NonNull String variables) { this.variables = variables; }
  
  private static List<String> convertStringToList(@NonNull String variablesCommaSeparated) {
    String[] variablesArray = variablesCommaSeparated.split("\\s+");
    List<String> variables = new ArrayList<String>();
    for (String nameFragment : variablesArray) {
      if (StringUtils.isNotBlank(nameFragment))
        variables.add(nameFragment.toLowerCase(Locale.ENGLISH)); 
    } 
    Collections.sort(variables);
    return variables;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public String getVariables() { return this.variables; }
  
  @DataBoundSetter
  public void setRetainCharacteristicEnvVars(boolean retainCharacteristicEnvVars) { this.retainCharacteristicEnvVars = retainCharacteristicEnvVars; }
  
  public boolean isRetainCharacteristicEnvVars() { return this.retainCharacteristicEnvVars; }
  
  private List<String> variablesToRetain() {
    List<String> vars = new ArrayList<String>(convertStringToList(this.variables));
    if (isRetainCharacteristicEnvVars())
      vars.addAll(CHARACTERISTIC_ENV_VARS); 
    return vars;
  }
  
  public void filter(@NonNull EnvVars envVars, @NonNull EnvVarsFilterRuleContext context) {
    Map<String, String> systemEnvVars = EnvVars.masterEnvVars;
    List<String> variablesRemoved = new ArrayList<String>();
    List<String> variablesReset = new ArrayList<String>();
    List<String> variables = variablesToRetain();
    for (Iterator<Map.Entry<String, String>> iterator = envVars.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry<String, String> entry = (Map.Entry)iterator.next();
      String variableName = (String)entry.getKey();
      String variableValue = (String)entry.getValue();
      if (!variables.contains(variableName.toLowerCase(Locale.ENGLISH))) {
        String systemValue = (String)systemEnvVars.get(variableName);
        if (systemValue == null) {
          variablesRemoved.add(variableName);
          iterator.remove();
          continue;
        } 
        switch (null.$SwitchMap$jenkins$tasks$filters$impl$RetainVariablesLocalRule$ProcessVariablesHandling[this.processVariablesHandling.ordinal()]) {
          case 1:
            if (!systemValue.equals(variableValue))
              variablesReset.add(variableName); 
            continue;
          case 2:
            variablesRemoved.add(variableName);
            iterator.remove();
            continue;
        } 
        throw new AssertionError("Unknown process variables handling: " + this.processVariablesHandling);
      } 
    } 
    if (!variablesRemoved.isEmpty())
      context.getTaskListener().getLogger().println(Messages.RetainVariablesLocalRule_RemovalMessage(getDescriptor().getDisplayName(), String.join(", ", variablesRemoved))); 
    if (!variablesReset.isEmpty()) {
      variablesReset.forEach(variableName -> envVars.put(variableName, (String)systemEnvVars.get(variableName)));
      context.getTaskListener().getLogger().println(Messages.RetainVariablesLocalRule_ResetMessage(getDescriptor().getDisplayName(), String.join(", ", variablesReset)));
    } 
  }
  
  public ProcessVariablesHandling getProcessVariablesHandling() { return this.processVariablesHandling; }
  
  @DataBoundSetter
  public void setProcessVariablesHandling(ProcessVariablesHandling processVariablesHandling) { this.processVariablesHandling = processVariablesHandling; }
}
