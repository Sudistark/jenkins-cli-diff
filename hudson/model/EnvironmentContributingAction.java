package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Util;
import org.kohsuke.accmod.Restricted;

public interface EnvironmentContributingAction extends Action {
  default void buildEnvironment(@NonNull Run<?, ?> run, @NonNull EnvVars env) {
    if (run instanceof AbstractBuild && 
      Util.isOverridden(EnvironmentContributingAction.class, 
        getClass(), "buildEnvVars", new Class[] { AbstractBuild.class, EnvVars.class }))
      buildEnvVars((AbstractBuild)run, env); 
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.ProtectedExternally.class})
  default void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
    if (Util.isOverridden(EnvironmentContributingAction.class, 
        getClass(), "buildEnvironment", new Class[] { Run.class, EnvVars.class }))
      buildEnvironment(build, env); 
  }
}
