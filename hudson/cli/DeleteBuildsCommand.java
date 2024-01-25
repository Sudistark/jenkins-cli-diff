package hudson.cli;

import hudson.Extension;
import hudson.model.Run;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class DeleteBuildsCommand extends RunRangeCommand {
  public String getShortDescription() { return Messages.DeleteBuildsCommand_ShortDescription(); }
  
  protected void printUsageSummary(PrintStream stderr) { stderr.println("Delete build records of a specified job, possibly in a bulk. "); }
  
  protected int act(List<Run<?, ?>> builds) throws IOException {
    this.job.checkPermission(Run.DELETE);
    HashSet<Integer> hsBuilds = new HashSet<Integer>();
    for (Run<?, ?> build : builds) {
      if (!hsBuilds.contains(Integer.valueOf(build.number))) {
        build.delete();
        hsBuilds.add(Integer.valueOf(build.number));
      } 
    } 
    this.stdout.println("Deleted " + hsBuilds.size() + " builds");
    return 0;
  }
}
