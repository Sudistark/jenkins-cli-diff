package jenkins.tools;

import hudson.Extension;
import jenkins.management.Messages;
import jenkins.model.GlobalConfigurationCategory;

@Extension
public class ToolConfigurationCategory extends GlobalConfigurationCategory {
  public String getShortDescription() { return Messages.ConfigureTools_Description(); }
  
  public String getDisplayName() { return Messages.ConfigureTools_DisplayName(); }
}
