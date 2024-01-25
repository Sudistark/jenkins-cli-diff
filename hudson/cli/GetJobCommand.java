package hudson.cli;

import hudson.Extension;
import hudson.model.AbstractItem;
import org.kohsuke.args4j.Argument;

@Extension
public class GetJobCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job", required = true)
  public AbstractItem job;
  
  public String getShortDescription() { return Messages.GetJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.job.writeConfigDotXml(this.stdout);
    return 0;
  }
}
