package hudson.triggers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Item;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import jenkins.triggers.SCMTriggerItem;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class SCMTrigger extends Trigger<Item> {
  private boolean ignorePostCommitHooks;
  
  @DataBoundConstructor
  public SCMTrigger(String scmpoll_spec) { super(scmpoll_spec); }
  
  @Deprecated
  public SCMTrigger(String scmpoll_spec, boolean ignorePostCommitHooks) {
    super(scmpoll_spec);
    this.ignorePostCommitHooks = ignorePostCommitHooks;
  }
  
  public boolean isIgnorePostCommitHooks() { return this.ignorePostCommitHooks; }
  
  @DataBoundSetter
  public void setIgnorePostCommitHooks(boolean ignorePostCommitHooks) { this.ignorePostCommitHooks = ignorePostCommitHooks; }
  
  public String getScmpoll_spec() { return getSpec(); }
  
  public void run() {
    if (this.job == null)
      return; 
    run(null);
  }
  
  public void run(Action[] additionalActions) {
    if (this.job == null)
      return; 
    DescriptorImpl d = getDescriptor();
    LOGGER.fine("Scheduling a polling for " + this.job);
    if (d.synchronousPolling) {
      LOGGER.fine("Running the trigger directly without threading, as it's already taken care of by Trigger.Cron");
      (new Runner(this, additionalActions)).run();
    } else {
      LOGGER.fine("scheduling the trigger to (asynchronously) run");
      d.queue.execute(new Runner(this, additionalActions));
      d.clogCheck();
    } 
  }
  
  public DescriptorImpl getDescriptor() { return (DescriptorImpl)super.getDescriptor(); }
  
  public Collection<? extends Action> getProjectActions() {
    if (this.job == null)
      return Collections.emptyList(); 
    return Set.of(new SCMAction(this));
  }
  
  public File getLogFile() { return new File(((Item)Objects.requireNonNull(this.job)).getRootDir(), "scm-polling.log"); }
  
  private static final Logger LOGGER = Logger.getLogger(SCMTrigger.class.getName());
  
  private SCMTriggerItem job() { return SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(this.job); }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static long STARVATION_THRESHOLD = SystemProperties.getLong(SCMTrigger.class.getName() + ".starvationThreshold", Long.valueOf(TimeUnit.HOURS.toMillis(1L))).longValue();
}
