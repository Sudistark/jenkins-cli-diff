package jenkins.triggers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.model.queue.QueueTaskFuture;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import java.util.Collection;
import jenkins.model.ParameterizedJobMixIn;

public interface SCMTriggerItem {
  Item asItem();
  
  int getNextBuildNumber();
  
  int getQuietPeriod();
  
  @CheckForNull
  QueueTaskFuture<?> scheduleBuild2(int paramInt, Action... paramVarArgs);
  
  @NonNull
  PollingResult poll(@NonNull TaskListener paramTaskListener);
  
  @CheckForNull
  SCMTrigger getSCMTrigger();
  
  @NonNull
  Collection<? extends SCM> getSCMs();
  
  default boolean schedulePolling() {
    if (this instanceof ParameterizedJobMixIn.ParameterizedJob && ((ParameterizedJobMixIn.ParameterizedJob)this).isDisabled())
      return false; 
    SCMTrigger scmt = getSCMTrigger();
    if (scmt == null)
      return false; 
    scmt.run();
    return true;
  }
}
