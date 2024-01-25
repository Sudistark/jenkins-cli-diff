package hudson.cli;

import hudson.Extension;
import hudson.model.View;
import javax.xml.transform.stream.StreamSource;
import org.kohsuke.args4j.Argument;

@Extension
public class UpdateViewCommand extends CLICommand {
  @Argument(usage = "Name of the view to update", required = true)
  private View view;
  
  public String getShortDescription() { return Messages.UpdateViewCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.view.updateByXml(new StreamSource(this.stdin));
    return 0;
  }
}
