package hudson.tasks;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.Fingerprint;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.RunList;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import jenkins.model.DependencyDeclarer;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Fingerprinter extends Recorder implements Serializable, DependencyDeclarer, SimpleBuildStep {
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean enableFingerprintsInDependencyGraph = SystemProperties.getBoolean(Fingerprinter.class.getName() + ".enableFingerprintsInDependencyGraph");
  
  private final String targets;
  
  private String excludes;
  
  private Boolean defaultExcludes;
  
  private Boolean caseSensitive;
  
  @Deprecated
  Boolean recordBuildArtifacts;
  
  @DataBoundConstructor
  public Fingerprinter(String targets) {
    this.excludes = null;
    this.defaultExcludes = Boolean.valueOf(true);
    this.caseSensitive = Boolean.valueOf(true);
    this.targets = targets;
  }
  
  @DataBoundSetter
  public void setExcludes(String excludes) { this.excludes = Util.fixEmpty(excludes); }
  
  @DataBoundSetter
  public void setDefaultExcludes(boolean defaultExcludes) { this.defaultExcludes = Boolean.valueOf(defaultExcludes); }
  
  @DataBoundSetter
  public void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = Boolean.valueOf(caseSensitive); }
  
  @Deprecated
  public Fingerprinter(String targets, boolean recordBuildArtifacts) {
    this(targets);
    this.recordBuildArtifacts = Boolean.valueOf(recordBuildArtifacts);
  }
  
  public String getTargets() { return this.targets; }
  
  public String getExcludes() { return this.excludes; }
  
  public boolean getDefaultExcludes() { return this.defaultExcludes.booleanValue(); }
  
  public boolean getCaseSensitive() { return this.caseSensitive.booleanValue(); }
  
  private Object readResolve() {
    if (this.defaultExcludes == null)
      this.defaultExcludes = Boolean.valueOf(true); 
    if (this.caseSensitive == null)
      this.caseSensitive = Boolean.valueOf(true); 
    return this;
  }
  
  @Deprecated
  public boolean getRecordBuildArtifacts() { return (this.recordBuildArtifacts != null && this.recordBuildArtifacts.booleanValue()); }
  
  public void perform(Run<?, ?> build, FilePath workspace, EnvVars environment, Launcher launcher, TaskListener listener) throws InterruptedException {
    try {
      listener.getLogger().println(Messages.Fingerprinter_Recording());
      Map<String, String> record = new HashMap<String, String>();
      if (this.targets.length() != 0) {
        String expandedTargets = this.targets;
        if (build instanceof hudson.model.AbstractBuild)
          expandedTargets = environment.expand(expandedTargets); 
        record(build, workspace, listener, record, expandedTargets);
      } 
      FingerprintAction fingerprintAction = (FingerprintAction)build.getAction(FingerprintAction.class);
      if (fingerprintAction != null) {
        fingerprintAction.add(record);
      } else {
        build.addAction(new FingerprintAction(build, record));
      } 
      if (enableFingerprintsInDependencyGraph)
        Jenkins.get().rebuildDependencyGraphAsync(); 
    } catch (IOException e) {
      Functions.printStackTrace(e, listener.error(Messages.Fingerprinter_Failed()));
      build.setResult(Result.FAILURE);
    } 
  }
  
  public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }
  
  public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
    if (enableFingerprintsInDependencyGraph) {
      RunList builds = owner.getBuilds();
      Set<String> seenUpstreamProjects = new HashSet<String>();
      for (Object build1 : builds) {
        Run build = (Run)build1;
        for (FingerprintAction action : build.getActions(FingerprintAction.class)) {
          for (AbstractProject key : action.getDependencies().keySet()) {
            if (key == owner)
              continue; 
            AbstractProject p = key;
            if (key.getClass().getName().equals("hudson.matrix.MatrixConfiguration"))
              p = key.getRootProject(); 
            if (seenUpstreamProjects.contains(p.getName()))
              continue; 
            seenUpstreamProjects.add(p.getName());
            graph.addDependency(new Object(this, p, owner));
          } 
        } 
      } 
    } 
  }
  
  private void record(Run<?, ?> build, FilePath ws, TaskListener listener, Map<String, String> record, String targets) throws IOException, InterruptedException {
    for (Record r : (List)ws.act(new FindRecords(targets, this.excludes, this.defaultExcludes.booleanValue(), this.caseSensitive.booleanValue(), build.getTimeInMillis()))) {
      Fingerprint fp = r.addRecord(build);
      fp.addFor(build);
      record.put(r.relativePath, fp.getHashString());
    } 
  }
  
  private static final Logger logger = Logger.getLogger(Fingerprinter.class.getName());
  
  private static final long serialVersionUID = 1L;
}
