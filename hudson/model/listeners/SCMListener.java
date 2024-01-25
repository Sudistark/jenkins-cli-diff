package hudson.model.listeners;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;

public abstract class SCMListener implements ExtensionPoint {
  public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, @CheckForNull File changelogFile, @CheckForNull SCMRevisionState pollingBaseline) throws Exception {}
  
  public void onChangeLogParsed(Run<?, ?> build, SCM scm, TaskListener listener, ChangeLogSet<?> changelog) throws Exception {
    if (build instanceof AbstractBuild && listener instanceof BuildListener && Util.isOverridden(SCMListener.class, getClass(), "onChangeLogParsed", new Class[] { AbstractBuild.class, BuildListener.class, ChangeLogSet.class }))
      onChangeLogParsed((AbstractBuild)build, (BuildListener)listener, changelog); 
  }
  
  @Deprecated
  public void onChangeLogParsed(AbstractBuild<?, ?> build, BuildListener listener, ChangeLogSet<?> changelog) throws Exception {
    if (Util.isOverridden(SCMListener.class, getClass(), "onChangeLogParsed", new Class[] { Run.class, SCM.class, TaskListener.class, ChangeLogSet.class }))
      onChangeLogParsed(build, build.getProject().getScm(), listener, changelog); 
  }
  
  public static Collection<? extends SCMListener> all() {
    j = Jenkins.getInstanceOrNull();
    if (j == null)
      return Collections.emptySet(); 
    List<SCMListener> r = new ArrayList<SCMListener>(j.getExtensionList(SCMListener.class));
    for (SCMListener l : j.getSCMListeners())
      r.add(l); 
    return r;
  }
  
  @Deprecated
  public final void register() { Jenkins.get().getSCMListeners().add(this); }
  
  @Deprecated
  public final boolean unregister() { return Jenkins.get().getSCMListeners().remove(this); }
}
