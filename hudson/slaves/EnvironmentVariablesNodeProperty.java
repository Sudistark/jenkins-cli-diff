package hudson.slaves;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.kohsuke.stapler.DataBoundConstructor;

public class EnvironmentVariablesNodeProperty extends NodeProperty<Node> {
  private final EnvVars envVars;
  
  @DataBoundConstructor
  public EnvironmentVariablesNodeProperty(List<Entry> env) { this.envVars = toMap(env); }
  
  public EnvironmentVariablesNodeProperty(Entry... env) { this(Arrays.asList(env)); }
  
  public EnvVars getEnvVars() { return this.envVars; }
  
  public List<Entry> getEnv() { return (List)this.envVars.entrySet().stream().map(x$0 -> new Entry(x$0)).collect(Collectors.toList()); }
  
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException { return Environment.create(this.envVars); }
  
  public void buildEnvVars(EnvVars env, TaskListener listener) throws IOException, InterruptedException { env.putAll(this.envVars); }
  
  private static EnvVars toMap(List<Entry> entries) {
    EnvVars map = new EnvVars();
    if (entries != null)
      for (Entry entry : entries)
        map.put(entry.key, entry.value);  
    return map;
  }
}
