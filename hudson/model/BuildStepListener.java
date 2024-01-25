package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.tasks.BuildStep;

public abstract class BuildStepListener implements ExtensionPoint {
  public abstract void started(AbstractBuild paramAbstractBuild, BuildStep paramBuildStep, BuildListener paramBuildListener);
  
  public abstract void finished(AbstractBuild paramAbstractBuild, BuildStep paramBuildStep, BuildListener paramBuildListener, boolean paramBoolean);
  
  public static ExtensionList<BuildStepListener> all() { return ExtensionList.lookup(BuildStepListener.class); }
}
