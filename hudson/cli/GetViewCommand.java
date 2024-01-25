package hudson.cli;

import hudson.Extension;
import hudson.model.View;
import org.kohsuke.args4j.Argument;

@Extension
public class GetViewCommand extends CLICommand {
  @Argument(usage = "Name of the view to obtain", required = true)
  private View view;
  
  public String getShortDescription() { return Messages.GetViewCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.view.checkPermission(View.READ);
    this.view.writeXml(this.stdout);
    return 0;
  }
}
