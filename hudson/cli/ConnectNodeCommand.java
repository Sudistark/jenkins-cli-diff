package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Computer;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class ConnectNodeCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Agent name, or empty string for built-in node; comma-separated list is supported", required = true, multiValued = true)
  private List<String> nodes;
  
  @Option(name = "-f", usage = "Cancel any currently pending connect operation and retry from scratch")
  public boolean force = false;
  
  private static final Logger LOGGER = Logger.getLogger(ConnectNodeCommand.class.getName());
  
  public String getShortDescription() { return Messages.ConnectNodeCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    HashSet<String> hs = new HashSet<String>(this.nodes);
    for (String node_s : hs) {
      try {
        Computer computer = Computer.resolveForCLI(node_s);
        computer.cliConnect(this.force);
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
