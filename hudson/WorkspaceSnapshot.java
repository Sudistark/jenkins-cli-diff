package hudson;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.TaskListener;
import java.io.IOException;

@Deprecated
public abstract class WorkspaceSnapshot implements Action {
  public abstract void restoreTo(AbstractBuild<?, ?> paramAbstractBuild, FilePath paramFilePath, TaskListener paramTaskListener) throws IOException, InterruptedException;
  
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return "Workspace"; }
  
  public String getUrlName() { return "workspace"; }
}
