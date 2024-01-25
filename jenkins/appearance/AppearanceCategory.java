package jenkins.appearance;

import hudson.Extension;
import jenkins.model.GlobalConfigurationCategory;

@Extension
public class AppearanceCategory extends GlobalConfigurationCategory {
  public String getShortDescription() { return Messages.AppearanceCategory_DisplayName(); }
  
  public String getDisplayName() { return Messages.AppearanceCategory_Description(); }
}
