package hudson.cli;

import hudson.Extension;
import jenkins.model.Jenkins;

@Extension
public class SessionIdCommand extends CLICommand {
  public String getShortDescription() { return Messages.SessionIdCommand_ShortDescription(); }
  
  protected int run() {
    this.stdout.println(Jenkins.SESSION_HASH);
    return 0;
  }
}
