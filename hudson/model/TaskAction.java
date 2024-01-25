package hudson.model;

import hudson.console.AnnotatedLargeText;
import hudson.security.ACL;
import hudson.security.Permission;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.LargeText;
import org.kohsuke.stapler.interceptor.RequirePOST;

public abstract class TaskAction extends AbstractModelObject implements Action {
  protected WeakReference<AnnotatedLargeText> log;
  
  protected abstract Permission getPermission();
  
  protected abstract ACL getACL();
  
  public abstract String getIconFileName();
  
  @Deprecated
  public LargeText getLog() { return obtainLog(); }
  
  public AnnotatedLargeText obtainLog() {
    WeakReference<AnnotatedLargeText> l = this.log;
    if (l == null)
      return null; 
    return (AnnotatedLargeText)l.get();
  }
  
  public String getSearchUrl() { return getUrlName(); }
  
  public TaskThread getWorkerThread() { return this.workerThread; }
  
  public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
    AnnotatedLargeText text = obtainLog();
    if (text != null) {
      text.doProgressText(req, rsp);
      return;
    } 
    rsp.setStatus(200);
  }
  
  public void doProgressiveHtml(StaplerRequest req, StaplerResponse rsp) throws IOException {
    AnnotatedLargeText text = obtainLog();
    if (text != null) {
      text.doProgressiveHtml(req, rsp);
      return;
    } 
    rsp.setStatus(200);
  }
  
  @RequirePOST
  public void doClearError(StaplerRequest req, StaplerResponse rsp) throws IOException {
    getACL().checkPermission(getPermission());
    if (this.workerThread != null && !this.workerThread.isRunning())
      this.workerThread = null; 
    rsp.sendRedirect(".");
  }
}
