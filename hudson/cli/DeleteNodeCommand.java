package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Node;
import java.util.HashSet;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class DeleteNodeCommand extends CLICommand {
  @Argument(usage = "Names of nodes to delete", required = true, multiValued = true)
  private List<String> nodes;
  
  public String getShortDescription() { return Messages.DeleteNodeCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    Jenkins jenkins = Jenkins.get();
    HashSet<String> hs = new HashSet<String>(this.nodes);
    for (String node_s : hs) {
      try {
        Node node = jenkins.getNode(node_s);
        if (node == null)
          throw new IllegalArgumentException("No such node '" + node_s + "'"); 
        node.toComputer().doDoDelete();
      } catch (Exception e) {
        if (hs.size() == 1)
          throw e; 
        String errorMsg = node_s + ": " + node_s;
        this.stderr.println(errorMsg);
        errorOccurred = true;
      } 
    } 
    if (errorOccurred)
      throw new AbortException("Error occurred while performing this command, see previous stderr output."); 
    return 0;
  }
}
