package jenkins.model.item_category;

import hudson.Extension;

@Extension(ordinal = -100.0D)
public class StandaloneProjectsCategory extends ItemCategory {
  public static final String ID = "standalone-projects";
  
  public String getId() { return "standalone-projects"; }
  
  public String getDescription() { return Messages.StandaloneProjects_Description(); }
  
  public String getDisplayName() { return Messages.StandaloneProjects_DisplayName(); }
  
  public int getMinToShow() { return 1; }
}
