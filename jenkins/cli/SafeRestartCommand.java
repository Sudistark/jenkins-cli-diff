package jenkins.cli;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.cli.Messages;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Option;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class SafeRestartCommand extends CLICommand {
  private static final Logger LOGGER = Logger.getLogger(SafeRestartCommand.class.getName());
  
  @Option(name = "-message", usage = "Message for safe restart that will be visible to users")
  public String message = null;
  
  public String getShortDescription() { return Messages.SafeRestartCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins.get().doSafeRestart(null, this.message);
    return 0;
  }
}
