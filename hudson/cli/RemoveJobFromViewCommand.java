package hudson.cli;

import hudson.Extension;
import hudson.model.DirectlyModifiableView;
import hudson.model.TopLevelItem;
import hudson.model.View;
import java.util.List;
import org.kohsuke.args4j.Argument;

@Extension
public class RemoveJobFromViewCommand extends CLICommand {
  @Argument(usage = "Name of the view", required = true, index = 0)
  private View view;
  
  @Argument(usage = "Job names", required = true, index = 1)
  private List<TopLevelItem> jobs;
  
  public String getShortDescription() { return Messages.RemoveJobFromViewCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.view.checkPermission(View.CONFIGURE);
    if (!(this.view instanceof DirectlyModifiableView))
      throw new IllegalStateException("'" + this.view.getDisplayName() + "' view can not be modified directly"); 
    for (TopLevelItem job : this.jobs)
      ((DirectlyModifiableView)this.view).remove(job); 
    return 0;
  }
}
