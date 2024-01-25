package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Executor;
import hudson.model.Run;
import hudson.model.queue.CauseOfBlockage;

public class BlockedBecauseOfBuildInProgress extends CauseOfBlockage {
  @NonNull
  private final Run<?, ?> build;
  
  public BlockedBecauseOfBuildInProgress(@NonNull Run<?, ?> build) { this.build = build; }
  
  public String getShortDescription() {
    Executor e = this.build.getExecutor();
    String eta = "";
    if (e != null)
      eta = Messages.BlockedBecauseOfBuildInProgress_ETA(e.getEstimatedRemainingTime()); 
    int lbn = this.build.getNumber();
    return Messages.BlockedBecauseOfBuildInProgress_shortDescription(Integer.valueOf(lbn), eta);
  }
}
