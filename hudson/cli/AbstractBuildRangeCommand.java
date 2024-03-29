package hudson.cli;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Fingerprint;
import java.io.IOException;
import java.util.List;
import org.kohsuke.args4j.Argument;

@Deprecated
public abstract class AbstractBuildRangeCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job to build", required = true, index = 0)
  public AbstractProject<?, ?> job;
  
  @Argument(metaVar = "RANGE", usage = "Range of the build records to delete. 'N-M', 'N,M', or 'N'", required = true, index = 1)
  public String range;
  
  protected int run() throws Exception {
    Fingerprint.RangeSet rs = Fingerprint.RangeSet.fromString(this.range, false);
    return act(this.job.getBuilds(rs));
  }
  
  protected abstract int act(List<AbstractBuild<?, ?>> paramList) throws IOException;
}
