package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;

public abstract class WorkspaceListener implements ExtensionPoint {
  public void afterDelete(AbstractProject project) {}
  
  public void beforeUse(AbstractBuild b, FilePath workspace, BuildListener listener) {}
  
  public static ExtensionList<WorkspaceListener> all() { return ExtensionList.lookup(WorkspaceListener.class); }
}
