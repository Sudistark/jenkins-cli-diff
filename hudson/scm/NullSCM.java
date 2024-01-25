package hudson.scm;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;

public class NullSCM extends SCM {
  public SCMRevisionState calcRevisionsFromBuild(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException { return null; }
  
  public PollingResult compareRemoteRevisionWith(Job<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException { return PollingResult.NO_CHANGES; }
  
  public void checkout(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
    if (changelogFile != null)
      createEmptyChangeLog(changelogFile, listener, "log"); 
  }
  
  public ChangeLogParser createChangeLogParser() { return NullChangeLogParser.INSTANCE; }
}
