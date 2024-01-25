package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Run;
import java.io.IOException;
import java.util.Map;
import jenkins.util.VirtualFile;

public abstract class ArtifactManager {
  public abstract void onLoad(@NonNull Run<?, ?> paramRun);
  
  public abstract void archive(FilePath paramFilePath, Launcher paramLauncher, BuildListener paramBuildListener, Map<String, String> paramMap) throws IOException, InterruptedException;
  
  public abstract boolean delete() throws IOException, InterruptedException;
  
  public abstract VirtualFile root();
}
