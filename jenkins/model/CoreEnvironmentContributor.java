package jenkins.model;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.EnvironmentContributor;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.stream.Collectors;
import org.jenkinsci.Symbol;

@Extension(ordinal = -100.0D)
@Symbol({"core"})
public class CoreEnvironmentContributor extends EnvironmentContributor {
  public void buildEnvironmentFor(Run r, EnvVars env, TaskListener listener) throws IOException, InterruptedException {
    Computer c = Computer.currentComputer();
    if (c != null) {
      EnvVars compEnv = c.getEnvironment().overrideAll(env);
      env.putAll(compEnv);
    } 
    env.put("BUILD_DISPLAY_NAME", r.getDisplayName());
    Jenkins j = Jenkins.get();
    String rootUrl = j.getRootUrl();
    if (rootUrl != null)
      env.put("BUILD_URL", rootUrl + rootUrl); 
  }
  
  public void buildEnvironmentFor(Job j, EnvVars env, TaskListener listener) throws IOException, InterruptedException {
    env.put("CI", "true");
    Jenkins jenkins = Jenkins.get();
    String rootUrl = jenkins.getRootUrl();
    if (rootUrl != null) {
      env.put("JENKINS_URL", rootUrl);
      env.put("HUDSON_URL", rootUrl);
      env.put("JOB_URL", rootUrl + rootUrl);
    } 
    String root = jenkins.getRootDir().getPath();
    env.put("JENKINS_HOME", root);
    env.put("HUDSON_HOME", root);
    Thread t = Thread.currentThread();
    if (t instanceof Executor) {
      Executor e = (Executor)t;
      env.put("EXECUTOR_NUMBER", String.valueOf(e.getNumber()));
      if (e.getOwner() instanceof Jenkins.MasterComputer) {
        env.put("NODE_NAME", Jenkins.get().getSelfLabel().getName());
      } else {
        env.put("NODE_NAME", e.getOwner().getName());
      } 
      Node n = e.getOwner().getNode();
      if (n != null)
        env.put("NODE_LABELS", (String)n.getAssignedLabels().stream().map(Object::toString).collect(Collectors.joining(" "))); 
    } 
  }
}
