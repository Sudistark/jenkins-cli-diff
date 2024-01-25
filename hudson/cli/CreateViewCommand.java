package hudson.cli;

import hudson.Extension;
import hudson.model.Failure;
import hudson.model.View;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class CreateViewCommand extends CLICommand {
  @Argument(usage = "Name of the view to use instead of the one in XML")
  public String viewName = null;
  
  public String getShortDescription() { return Messages.CreateViewCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    View newView;
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(View.CREATE);
    try {
      newView = View.createViewFromXML(this.viewName, this.stdin);
    } catch (Failure ex) {
      throw new IllegalArgumentException("Invalid view name: " + ex.getMessage());
    } 
    String newName = newView.getViewName();
    if (jenkins.getView(newName) != null)
      throw new IllegalStateException("View '" + newName + "' already exists"); 
    jenkins.addView(newView);
    return 0;
  }
}
