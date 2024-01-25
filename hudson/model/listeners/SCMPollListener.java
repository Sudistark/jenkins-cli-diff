package hudson.model.listeners;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractProject;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import jenkins.util.Listeners;

public abstract class SCMPollListener implements ExtensionPoint {
  public void onBeforePolling(AbstractProject<?, ?> project, TaskListener listener) {}
  
  public void onPollingSuccess(AbstractProject<?, ?> project, TaskListener listener, PollingResult result) {}
  
  public void onPollingFailed(AbstractProject<?, ?> project, TaskListener listener, Throwable exception) {}
  
  public static void fireBeforePolling(AbstractProject<?, ?> project, TaskListener listener) { Listeners.notify(SCMPollListener.class, true, l -> l.onBeforePolling(project, listener)); }
  
  public static void firePollingSuccess(AbstractProject<?, ?> project, TaskListener listener, PollingResult result) { Listeners.notify(SCMPollListener.class, true, l -> l.onPollingSuccess(project, listener, result)); }
  
  public static void firePollingFailed(AbstractProject<?, ?> project, TaskListener listener, Throwable exception) { Listeners.notify(SCMPollListener.class, true, l -> l.onPollingFailed(project, listener, exception)); }
  
  public static ExtensionList<SCMPollListener> all() { return ExtensionList.lookup(SCMPollListener.class); }
}
