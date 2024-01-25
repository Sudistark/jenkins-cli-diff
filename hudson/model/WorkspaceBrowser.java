package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.slaves.WorkspaceList;

public abstract class WorkspaceBrowser implements ExtensionPoint {
  private final WorkspaceList workspaceList = new WorkspaceList();
  
  @CheckForNull
  public abstract FilePath getWorkspace(Job paramJob);
  
  final WorkspaceList getWorkspaceList() { return this.workspaceList; }
}
