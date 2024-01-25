package hudson.cli;

import hudson.Extension;
import hudson.model.Node;
import org.kohsuke.args4j.Argument;

@Extension
public class WaitNodeOfflineCommand extends CLICommand {
  @Argument(metaVar = "NODE", usage = "Name of the node", required = true)
  public Node node;
  
  public String getShortDescription() { return Messages.WaitNodeOfflineCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.node.toComputer().waitUntilOffline();
    return 0;
  }
}
