package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Computer;
import java.util.HashSet;
import java.util.List;
import org.kohsuke.args4j.Argument;

@Extension
public class OnlineNodeCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Agent name, or empty string for built-in node", required = true, multiValued = true)
  private List<String> nodes;
  
  public String getShortDescription() { return Messages.OnlineNodeCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    HashSet<String> hs = new HashSet<String>(this.nodes);
    for (String node_s : hs) {
      try {
        Computer computer = Computer.resolveForCLI(node_s);
        computer.cliOnline();
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
