package hudson.cli;

import hudson.model.Fingerprint;
import hudson.model.Job;
import hudson.model.Run;
import java.io.IOException;
import java.util.List;
import org.kohsuke.args4j.Argument;

public abstract class RunRangeCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job to build", required = true, index = 0)
  public Job<?, ?> job;
  
  @Argument(metaVar = "RANGE", usage = "Range of the build records to delete. 'N-M', 'N,M', or 'N'", required = true, index = 1)
  public String range;
  
  protected int run() throws Exception {
    Fingerprint.RangeSet rs = Fingerprint.RangeSet.fromString(this.range, false);
    return act(this.job.getBuilds(rs));
  }
  
  protected abstract int act(List<Run<?, ?>> paramList) throws IOException;
}
