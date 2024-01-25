package hudson;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import java.io.IOException;
import jenkins.model.Jenkins;

@Deprecated
public abstract class FileSystemProvisioner extends Object implements Describable<FileSystemProvisioner> {
  public abstract void prepareWorkspace(AbstractBuild<?, ?> paramAbstractBuild, FilePath paramFilePath, TaskListener paramTaskListener) throws IOException, InterruptedException;
  
  public abstract void discardWorkspace(AbstractProject<?, ?> paramAbstractProject, FilePath paramFilePath) throws IOException, InterruptedException;
  
  public abstract WorkspaceSnapshot snapshot(AbstractBuild<?, ?> paramAbstractBuild, FilePath paramFilePath, String paramString, TaskListener paramTaskListener) throws IOException, InterruptedException;
  
  public Descriptor getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static final FileSystemProvisioner DEFAULT = new Default();
}
