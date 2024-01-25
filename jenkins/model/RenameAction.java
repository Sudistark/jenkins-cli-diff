package jenkins.model;

import hudson.model.Action;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class RenameAction implements Action {
  public String getIconFileName() { return "notepad.png"; }
  
  public String getDisplayName() { return "Rename"; }
  
  public String getUrlName() { return "confirm-rename"; }
}
