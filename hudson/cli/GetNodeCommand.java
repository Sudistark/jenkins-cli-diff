package hudson.cli;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import java.io.IOException;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class GetNodeCommand extends CLICommand {
  @Argument(metaVar = "NODE", usage = "Name of the node", required = true)
  public Node node;
  
  public String getShortDescription() { return Messages.GetNodeCommand_ShortDescription(); }
  
  protected int run() throws IOException {
    this.node.checkPermission(Computer.EXTENDED_READ);
    Jenkins.XSTREAM2.toXMLUTF8(this.node, this.stdout);
    return 0;
  }
}
