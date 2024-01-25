package jenkins.security.apitoken;

import hudson.Extension;
import hudson.model.PersistentDescriptor;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"apiToken"})
public class ApiTokenPropertyConfiguration extends GlobalConfiguration implements PersistentDescriptor {
  private boolean tokenGenerationOnCreationEnabled = false;
  
  private boolean creationOfLegacyTokenEnabled = false;
  
  private boolean usageStatisticsEnabled = true;
  
  public static ApiTokenPropertyConfiguration get() { return (ApiTokenPropertyConfiguration)GlobalConfiguration.all().get(ApiTokenPropertyConfiguration.class); }
  
  public boolean hasExistingConfigFile() { return getConfigFile().exists(); }
  
  public boolean isTokenGenerationOnCreationEnabled() { return this.tokenGenerationOnCreationEnabled; }
  
  public void setTokenGenerationOnCreationEnabled(boolean tokenGenerationOnCreationEnabled) {
    this.tokenGenerationOnCreationEnabled = tokenGenerationOnCreationEnabled;
    save();
  }
  
  public boolean isCreationOfLegacyTokenEnabled() { return this.creationOfLegacyTokenEnabled; }
  
  public void setCreationOfLegacyTokenEnabled(boolean creationOfLegacyTokenEnabled) {
    this.creationOfLegacyTokenEnabled = creationOfLegacyTokenEnabled;
    save();
  }
  
  public boolean isUsageStatisticsEnabled() { return this.usageStatisticsEnabled; }
  
  public void setUsageStatisticsEnabled(boolean usageStatisticsEnabled) {
    this.usageStatisticsEnabled = usageStatisticsEnabled;
    save();
  }
  
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class); }
}
