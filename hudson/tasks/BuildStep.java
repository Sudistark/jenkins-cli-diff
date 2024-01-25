package hudson.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.util.DescriptorList;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface BuildStep {
  boolean prebuild(AbstractBuild<?, ?> paramAbstractBuild, BuildListener paramBuildListener);
  
  boolean perform(AbstractBuild<?, ?> paramAbstractBuild, Launcher paramLauncher, BuildListener paramBuildListener) throws InterruptedException, IOException;
  
  @Deprecated
  Action getProjectAction(AbstractProject<?, ?> paramAbstractProject);
  
  @NonNull
  Collection<? extends Action> getProjectActions(AbstractProject<?, ?> paramAbstractProject);
  
  default BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.BUILD; }
  
  @Deprecated
  public static final List<Descriptor<Builder>> BUILDERS = new DescriptorList(Builder.class);
  
  @Deprecated
  public static final PublisherList PUBLISHERS = new PublisherList();
}
