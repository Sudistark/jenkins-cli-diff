package jenkins.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.kohsuke.accmod.Restricted;

public abstract class SimpleBuildWrapper extends BuildWrapper {
  public boolean requiresWorkspace() { return true; }
  
  public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
    if (!requiresWorkspace()) {
      setUp(context, build, listener, initialEnvironment);
      return;
    } 
    throw new AbstractMethodError("Unless a build wrapper is marked as not requiring a workspace context, you must implement the overload of the setUp() method that takes both a workspace and a launcher.");
  }
  
  public void setUp(Context context, Run<?, ?> build, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
    if (requiresWorkspace())
      throw new IllegalStateException("This build wrapper requires a workspace context, but none was provided."); 
    throw new AbstractMethodError("When a build wrapper is marked as not requiring a workspace context, you must implement the overload of the setUp() method that does not take a workspace or launcher.");
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public Context createContext() { return new Context(requiresWorkspace()); }
  
  protected boolean runPreCheckout() { return false; }
  
  public final BuildWrapper.Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    if (runPreCheckout())
      return new Object(this); 
    Context c = createContext();
    setUp(c, build, build.getWorkspace(), launcher, listener, build.getEnvironment(listener));
    return new EnvironmentWrapper(this, c, launcher);
  }
  
  public final void preCheckout(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    if (runPreCheckout()) {
      Context c = createContext();
      setUp(c, build, build.getWorkspace(), launcher, listener, build.getEnvironment(listener));
      build.getEnvironments().add(new EnvironmentWrapper(this, c, launcher));
    } 
  }
  
  @CheckForNull
  public ConsoleLogFilter createLoggerDecorator(@NonNull Run<?, ?> build) { return null; }
  
  public final OutputStream decorateLogger(AbstractBuild build, OutputStream logger) throws IOException, InterruptedException, Run.RunnerAbortedException {
    ConsoleLogFilter filter = createLoggerDecorator(build);
    return (filter != null) ? filter.decorateLogger(build, logger) : logger;
  }
  
  public Launcher decorateLauncher(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException { return super.decorateLauncher(build, launcher, listener); }
  
  public void makeBuildVariables(AbstractBuild build, Map<String, String> variables) { super.makeBuildVariables(build, variables); }
  
  public void makeSensitiveBuildVariables(AbstractBuild build, Set<String> sensitiveVariables) { super.makeSensitiveBuildVariables(build, sensitiveVariables); }
  
  public final Collection<? extends Action> getProjectActions(AbstractProject job) { return Collections.emptySet(); }
}
