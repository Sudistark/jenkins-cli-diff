package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.Messages;
import hudson.util.EditDistance;
import java.util.HashSet;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class OfflineNodeCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Agent name, or empty string for built-in node", required = true, multiValued = true)
  private List<String> nodes;
  
  @Option(name = "-m", usage = "Record the reason about why you are disconnecting this node")
  public String cause;
  
  public String getShortDescription() { return Messages.OfflineNodeCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    Jenkins jenkins = Jenkins.get();
    HashSet<String> hs = new HashSet<String>(this.nodes);
    List<String> names = null;
    for (String node_s : hs) {
      try {
        Computer computer = jenkins.getComputer(node_s);
        if (computer == null) {
          if (names == null)
            names = ComputerSet.getComputerNames(); 
          String adv = EditDistance.findNearest(node_s, names);
          throw new IllegalArgumentException((adv == null) ? 
              Messages.Computer_NoSuchSlaveExistsWithoutAdvice(node_s) : 
              Messages.Computer_NoSuchSlaveExists(node_s, adv));
        } 
        computer.cliOffline(this.cause);
      } catch (Exception e) {
        if (hs.size() == 1)
          throw e; 
        this.stderr.println(node_s + ": " + node_s);
        errorOccurred = true;
      } 
    } 
    if (errorOccurred)
      throw new AbortException("Error occurred while performing this command, see previous stderr output."); 
    return 0;
  }
}
