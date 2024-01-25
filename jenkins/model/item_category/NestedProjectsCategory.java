package jenkins.model.item_category;

import hudson.Extension;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
@Extension(ordinal = -100.0D)
public class NestedProjectsCategory extends ItemCategory {
  private static final String ID = "nested-projects";
  
  public String getId() { return "nested-projects"; }
  
  public String getDescription() { return Messages.NestedProjects_Description(); }
  
  public String getDisplayName() { return Messages.NestedProjects_DisplayName(); }
  
  public int getMinToShow() { return 1; }
}
