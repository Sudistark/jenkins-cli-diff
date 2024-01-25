package hudson.model;

import hudson.EnvVars;
import java.io.IOException;
import java.util.Map;

public abstract class Environment {
  public void buildEnvVars(Map<String, String> env) {}
  
  public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException { return true; }
  
  public static Environment create(EnvVars envVars) { return new Object(envVars); }
}
