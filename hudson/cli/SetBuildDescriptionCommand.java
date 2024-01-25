package hudson.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import java.io.Serializable;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;

@Extension
@SuppressFBWarnings(value = {"SE_NO_SERIALVERSIONID"}, justification = "The Serializable should be removed.")
public class SetBuildDescriptionCommand extends CLICommand implements Serializable {
  @Argument(metaVar = "JOB", usage = "Name of the job to build", required = true, index = 0)
  public Job<?, ?> job;
  
  @Argument(metaVar = "BUILD#", usage = "Number of the build", required = true, index = 1)
  public int number;
  
  @Argument(metaVar = "DESCRIPTION", required = true, usage = "Description to be set. '=' to read from stdin.", index = 2)
  public String description;
  
  public String getShortDescription() { return Messages.SetBuildDescriptionCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Run run = this.job.getBuildByNumber(this.number);
    if (run == null)
      throw new IllegalArgumentException("No such build #" + this.number); 
    run.checkPermission(Run.UPDATE);
    if ("=".equals(this.description))
      this.description = IOUtils.toString(this.stdin); 
    run.setDescription(this.description);
    return 0;
  }
}
