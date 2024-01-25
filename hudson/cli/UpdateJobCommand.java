package hudson.cli;

import hudson.Extension;
import hudson.model.AbstractItem;
import javax.xml.transform.stream.StreamSource;
import org.kohsuke.args4j.Argument;

@Extension
public class UpdateJobCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job", required = true)
  public AbstractItem job;
  
  public String getShortDescription() { return Messages.UpdateJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    this.job.updateByXml(new StreamSource(this.stdin));
    return 0;
  }
}
