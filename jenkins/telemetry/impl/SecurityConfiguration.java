package jenkins.telemetry.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.TcpSlaveAgentListener;
import hudson.security.csrf.CrumbIssuer;
import java.time.LocalDate;
import jenkins.model.Jenkins;
import jenkins.security.apitoken.ApiTokenPropertyConfiguration;
import jenkins.telemetry.Telemetry;
import net.sf.json.JSONObject;

@Extension
public class SecurityConfiguration extends Telemetry {
  @NonNull
  public String getDisplayName() { return "Basic information about security-related settings"; }
  
  @NonNull
  public LocalDate getStart() { return LocalDate.of(2023, 8, 1); }
  
  @NonNull
  public LocalDate getEnd() { return LocalDate.of(2023, 12, 1); }
  
  public JSONObject createContent() {
    Jenkins j = Jenkins.get();
    JSONObject o = new JSONObject();
    o.put("components", buildComponentInformation());
    o.put("authorizationStrategy", j.getAuthorizationStrategy().getClass().getName());
    o.put("securityRealm", j.getSecurityRealm().getClass().getName());
    CrumbIssuer crumbIssuer = j.getCrumbIssuer();
    o.put("crumbIssuer", (crumbIssuer == null) ? null : crumbIssuer.getClass().getName());
    o.put("markupFormatter", j.getMarkupFormatter().getClass().getName());
    TcpSlaveAgentListener tcpSlaveAgentListener = j.getTcpSlaveAgentListener();
    o.put("inboundAgentListener", (tcpSlaveAgentListener == null) ? null : Boolean.valueOf((tcpSlaveAgentListener.configuredPort != -1)));
    ApiTokenPropertyConfiguration apiTokenPropertyConfiguration = (ApiTokenPropertyConfiguration)ExtensionList.lookupSingleton(ApiTokenPropertyConfiguration.class);
    o.put("apiTokenCreationOfLegacyTokenEnabled", Boolean.valueOf(apiTokenPropertyConfiguration.isCreationOfLegacyTokenEnabled()));
    o.put("apiTokenTokenGenerationOnCreationEnabled", Boolean.valueOf(apiTokenPropertyConfiguration.isTokenGenerationOnCreationEnabled()));
    o.put("apiTokenUsageStatisticsEnabled", Boolean.valueOf(apiTokenPropertyConfiguration.isUsageStatisticsEnabled()));
    return o;
  }
}
