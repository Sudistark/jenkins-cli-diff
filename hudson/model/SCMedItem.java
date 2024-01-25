package hudson.model;

import hudson.scm.PollingResult;
import hudson.scm.SCM;

@Deprecated
public interface SCMedItem extends BuildableItem {
  SCM getScm();
  
  AbstractProject<?, ?> asProject();
  
  @Deprecated
  boolean pollSCMChanges(TaskListener paramTaskListener);
  
  PollingResult poll(TaskListener paramTaskListener);
}
