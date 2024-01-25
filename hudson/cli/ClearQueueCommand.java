package hudson.cli;

import hudson.Extension;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@Extension
public class ClearQueueCommand extends CLICommand {
  private static final Logger LOGGER = Logger.getLogger(ClearQueueCommand.class.getName());
  
  public String getShortDescription() { return Messages.ClearQueueCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins.get().getQueue().clear();
    return 0;
  }
}
