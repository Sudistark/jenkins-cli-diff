package hudson.cli;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class CreateNodeCommand extends CLICommand {
  @Argument(metaVar = "NODE", usage = "Name of the node")
  public String nodeName;
  
  public String getShortDescription() { return Messages.CreateNodeCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Computer.CREATE);
    Node newNode = (Node)Jenkins.XSTREAM2.fromXML(this.stdin);
    if (this.nodeName != null)
      newNode.setNodeName(this.nodeName); 
    if (jenkins.getNode(newNode.getNodeName()) != null)
      throw new IllegalStateException("Node '" + newNode.getNodeName() + "' already exists"); 
    jenkins.addNode(newNode);
    return 0;
  }
}
