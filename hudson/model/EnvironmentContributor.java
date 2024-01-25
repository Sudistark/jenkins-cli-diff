package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;

public abstract class EnvironmentContributor implements ExtensionPoint {
  public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {}
  
  public void buildEnvironmentFor(@NonNull Job j, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {}
  
  public static ExtensionList<EnvironmentContributor> all() { return ExtensionList.lookup(EnvironmentContributor.class); }
}
