package hudson.triggers;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.BuildableItem;
import org.kohsuke.stapler.DataBoundConstructor;

public class TimerTrigger extends Trigger<BuildableItem> {
  @DataBoundConstructor
  public TimerTrigger(@NonNull String spec) { super(spec); }
  
  public void run() {
    if (this.job == null)
      return; 
    ((BuildableItem)this.job).scheduleBuild(0, new TimerTriggerCause());
  }
}
