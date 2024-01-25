package hudson.cli;

import hudson.Extension;
import jenkins.model.Jenkins;

@Extension
public class VersionCommand extends CLICommand {
  public String getShortDescription() { return Messages.VersionCommand_ShortDescription(); }
  
  protected int run() {
    this.stdout.println(Jenkins.VERSION);
    return 0;
  }
}
