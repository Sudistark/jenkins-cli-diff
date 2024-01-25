package jenkins.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStep;
import java.io.IOException;

public interface SimpleBuildStep extends BuildStep {
  @Deprecated
  default void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException { perform(run, workspace, run.getEnvironment(listener), launcher, listener); }
  
  default void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
    if (!requiresWorkspace()) {
      perform(run, env, listener);
      return;
    } 
    if (Util.isOverridden(SimpleBuildStep.class, getClass(), "perform", new Class[] { Run.class, FilePath.class, Launcher.class, TaskListener.class })) {
      perform(run, workspace, launcher, listener);
    } else {
      throw new AbstractMethodError("Unless a build step is marked as not requiring a workspace context, you must implement the overload of the perform() method that takes both a workspace and a launcher.");
    } 
  }
  
  default void perform(@NonNull Run<?, ?> run, @NonNull EnvVars env, @NonNull TaskListener listener) throws InterruptedException, IOException {
    if (requiresWorkspace())
      throw new IllegalStateException("This build step requires a workspace context, but none was provided."); 
    throw new AbstractMethodError("When a build step is marked as not requiring a workspace context, you must implement the overload of the perform() method that does not take a workspace or launcher.");
  }
  
  default boolean requiresWorkspace() { return true; }
}
