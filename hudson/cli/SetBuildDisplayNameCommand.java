package hudson.cli;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import java.io.Serializable;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;

@Extension
public class SetBuildDisplayNameCommand extends CLICommand implements Serializable {
  private static final long serialVersionUID = 6665171784136358536L;
  
  @Argument(metaVar = "JOB", usage = "Name of the job to build", required = true, index = 0)
  public Job<?, ?> job;
  
  @Argument(metaVar = "BUILD#", usage = "Number of the build", required = true, index = 1)
  public int number;
  
  @Argument(metaVar = "DISPLAYNAME", required = true, usage = "DisplayName to be set. '-' to read from stdin.", index = 2)
  public String displayName;
  
  public String getShortDescription() { return Messages.SetBuildDisplayNameCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Run<?, ?> run = this.job.getBuildByNumber(this.number);
    if (run == null)
      throw new IllegalArgumentException("Build #" + this.number + " does not exist"); 
    run.checkPermission(Run.UPDATE);
    if ("-".equals(this.displayName))
      this.displayName = IOUtils.toString(this.stdin); 
    run.setDisplayName(this.displayName);
    return 0;
  }
}
