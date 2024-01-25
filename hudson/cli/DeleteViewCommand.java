package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.cli.handlers.ViewOptionHandler;
import hudson.model.View;
import hudson.model.ViewGroup;
import java.util.HashSet;
import java.util.List;
import org.kohsuke.args4j.Argument;

@Extension
public class DeleteViewCommand extends CLICommand {
  @Argument(usage = "View names to delete", required = true, multiValued = true)
  private List<String> views;
  
  public String getShortDescription() { return Messages.DeleteViewCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    HashSet<String> hs = new HashSet<String>(this.views);
    ViewOptionHandler voh = new ViewOptionHandler(null, null, null);
    for (String view_s : hs) {
      try {
        View view = voh.getView(view_s);
        if (view == null)
          throw new IllegalArgumentException("View name is empty"); 
        view.checkPermission(View.DELETE);
        ViewGroup group = view.getOwner();
        if (!group.canDelete(view))
          throw new IllegalStateException(String.format("%s does not allow to delete '%s' view", new Object[] { group
                  .getDisplayName(), view
                  .getViewName() })); 
        group.deleteView(view);
      } catch (Exception e) {
        if (hs.size() == 1)
          throw e; 
        String errorMsg = view_s + ": " + view_s;
        this.stderr.println(errorMsg);
        errorOccurred = true;
      } 
    } 
    if (errorOccurred)
      throw new AbortException("Error occurred while performing this command, see previous stderr output."); 
    return 0;
  }
}
