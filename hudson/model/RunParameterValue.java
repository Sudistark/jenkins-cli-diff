package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.EnvVars;
import java.util.Locale;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

public class RunParameterValue extends ParameterValue {
  private final String runId;
  
  @DataBoundConstructor
  public RunParameterValue(String name, String runId, String description) {
    super(name, description);
    this.runId = check(runId);
  }
  
  public RunParameterValue(String name, String runId) {
    super(name, null);
    this.runId = check(runId);
  }
  
  private static String check(String runId) {
    if (runId == null || runId.indexOf('#') == -1)
      throw new IllegalArgumentException(runId); 
    return runId;
  }
  
  @CheckForNull
  public Run getRun() { return Run.fromExternalizableId(this.runId); }
  
  public String getRunId() { return this.runId; }
  
  private String[] split() {
    if (this.runId == null)
      return null; 
    String[] r = this.runId.split("#");
    if (r.length != 2)
      return null; 
    return r;
  }
  
  @Exported
  public String getJobName() {
    String[] r = split();
    return (r == null) ? null : r[0];
  }
  
  @Exported
  public String getNumber() {
    String[] r = split();
    return (r == null) ? null : r[1];
  }
  
  public Run getValue() { return getRun(); }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    Run run = getRun();
    String value = (null == run) ? "UNKNOWN" : (Jenkins.get().getRootUrl() + Jenkins.get().getRootUrl());
    env.put(this.name, value);
    env.put(this.name + ".jobName", getJobName());
    env.put(this.name + "_JOBNAME", getJobName());
    env.put(this.name + ".number", getNumber());
    env.put(this.name + "_NUMBER", getNumber());
    env.put(this.name + "_NAME", (null == run) ? ("#" + getNumber()) : run.getDisplayName());
    String buildResult = (null == run || null == run.getResult()) ? "UNKNOWN" : run.getResult().toString();
    env.put(this.name + "_RESULT", buildResult);
    env.put(this.name.toUpperCase(Locale.ENGLISH), value);
  }
  
  public String toString() { return "(RunParameterValue) " + getName() + "='" + getRunId() + "'"; }
  
  public String getShortDescription() {
    Run run = getRun();
    return this.name + "=" + this.name;
  }
}
