package hudson.cli;

import hudson.Extension;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Option;

@Extension
public class QuietDownCommand extends CLICommand {
  private static final Logger LOGGER = Logger.getLogger(QuietDownCommand.class.getName());
  
  @Option(name = "-block", usage = "Block until the system really quiets down and no builds are running")
  public boolean block = false;
  
  @Option(name = "-timeout", usage = "If non-zero, only block up to the specified number of milliseconds")
  public int timeout = 0;
  
  @Option(name = "-reason", usage = "Reason for quiet down that will be visible to users")
  public String reason = null;
  
  public String getShortDescription() { return Messages.QuietDownCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins.get().doQuietDown(this.block, this.timeout, this.reason);
    return 0;
  }
}
