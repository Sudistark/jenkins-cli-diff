package hudson.cli;

import hudson.Extension;
import hudson.model.Node;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.args4j.Argument;

@Extension
public class UpdateNodeCommand extends CLICommand {
  @Argument(metaVar = "NODE", usage = "Name of the node", required = true)
  public Node node;
  
  public String getShortDescription() { return Messages.UpdateNodeCommand_ShortDescription(); }
  
  protected int run() throws IOException, ServletException {
    this.node.toComputer().updateByXml(this.stdin);
    return 0;
  }
}
