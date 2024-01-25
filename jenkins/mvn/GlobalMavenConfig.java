package jenkins.mvn;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.PersistentDescriptor;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.tools.ToolConfigurationCategory;
import org.jenkinsci.Symbol;

@Extension(ordinal = 50.0D)
@Symbol({"mavenGlobalConfig"})
public class GlobalMavenConfig extends GlobalConfiguration implements PersistentDescriptor {
  private SettingsProvider settingsProvider;
  
  private GlobalSettingsProvider globalSettingsProvider;
  
  @NonNull
  public ToolConfigurationCategory getCategory() { return (ToolConfigurationCategory)GlobalConfigurationCategory.get(ToolConfigurationCategory.class); }
  
  public void setGlobalSettingsProvider(GlobalSettingsProvider globalSettingsProvider) {
    this.globalSettingsProvider = globalSettingsProvider;
    save();
  }
  
  public void setSettingsProvider(SettingsProvider settingsProvider) {
    this.settingsProvider = settingsProvider;
    save();
  }
  
  public GlobalSettingsProvider getGlobalSettingsProvider() { return (this.globalSettingsProvider != null) ? this.globalSettingsProvider : new DefaultGlobalSettingsProvider(); }
  
  public SettingsProvider getSettingsProvider() { return (this.settingsProvider != null) ? this.settingsProvider : new DefaultSettingsProvider(); }
  
  @NonNull
  public static GlobalMavenConfig get() { return (GlobalMavenConfig)GlobalConfiguration.all().getInstance(GlobalMavenConfig.class); }
}
