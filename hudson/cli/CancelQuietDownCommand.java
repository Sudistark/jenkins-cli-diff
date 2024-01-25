package hudson.cli;

import hudson.Extension;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@Extension
public class CancelQuietDownCommand extends CLICommand {
  private static final Logger LOGGER = Logger.getLogger(CancelQuietDownCommand.class.getName());
  
  public String getShortDescription() { return Messages.CancelQuietDownCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins.get().doCancelQuietDown();
    return 0;
  }
}
