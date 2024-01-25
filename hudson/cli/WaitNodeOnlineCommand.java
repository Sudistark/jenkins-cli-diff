package hudson.cli;

import hudson.Extension;
import hudson.model.Node;
import org.kohsuke.args4j.Argument;

@Extension
public class WaitNodeOnlineCommand extends CLICommand {
  @Argument(metaVar = "NODE", usage = "Name of the node", required = true)
  public Node node;
  
  public String getShortDescription() { return Messages.WaitNodeOnlineCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.node.toComputer().waitUntilOnline();
    return 0;
  }
}
