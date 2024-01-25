package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import java.util.Map;
import java.util.TreeMap;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;
import org.springframework.security.access.AccessDeniedException;

@Extension
public class HelpCommand extends CLICommand {
  @Argument(metaVar = "COMMAND", usage = "Name of the command")
  public String command;
  
  public String getShortDescription() { return Messages.HelpCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    if (!Jenkins.get().hasPermission(Jenkins.READ))
      throw new AccessDeniedException("You must authenticate to access this Jenkins.\n" + 
          CLI.usage()); 
    if (this.command != null)
      return showCommandDetails(); 
    showAllCommands();
    return 0;
  }
  
  private int showAllCommands() throws Exception {
    Map<String, CLICommand> commands = new TreeMap<String, CLICommand>();
    for (CLICommand c : CLICommand.all())
      commands.put(c.getName(), c); 
    for (CLICommand c : commands.values()) {
      this.stderr.println("  " + c.getName());
      this.stderr.println("    " + c.getShortDescription());
    } 
    return 0;
  }
  
  private int showCommandDetails() throws Exception {
    CLICommand command = CLICommand.clone(this.command);
    if (command == null) {
      showAllCommands();
      throw new AbortException(String.format("No such command %s. Available commands are above. ", new Object[] { this.command }));
    } 
    command.printUsage(this.stderr, command.getCmdLineParser());
    return 0;
  }
}
