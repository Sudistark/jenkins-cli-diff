package hudson.tasks;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import java.io.IOException;

public static final abstract enum BuildStepMonitor {
  NONE, STEP, BUILD;
  
  public abstract boolean perform(BuildStep paramBuildStep, AbstractBuild paramAbstractBuild, Launcher paramLauncher, BuildListener paramBuildListener) throws IOException, InterruptedException;
}
