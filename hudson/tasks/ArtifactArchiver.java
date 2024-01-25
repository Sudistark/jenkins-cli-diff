package hudson.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import jenkins.util.BuildListenerAdapter;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ArtifactArchiver extends Recorder implements SimpleBuildStep {
  private static final Logger LOG = Logger.getLogger(ArtifactArchiver.class.getName());
  
  private String artifacts;
  
  private String excludes;
  
  @Deprecated
  private Boolean latestOnly;
  
  @NonNull
  private Boolean allowEmptyArchive;
  
  private boolean onlyIfSuccessful;
  
  private boolean fingerprint;
  
  @NonNull
  private Boolean defaultExcludes;
  
  @NonNull
  private Boolean caseSensitive;
  
  @NonNull
  private Boolean followSymlinks;
  
  @DataBoundConstructor
  public ArtifactArchiver(String artifacts) {
    this
      .defaultExcludes = Boolean.valueOf(true);
    this
      .caseSensitive = Boolean.valueOf(true);
    this
      .followSymlinks = Boolean.valueOf(true);
    this.artifacts = artifacts.trim();
    this.allowEmptyArchive = Boolean.valueOf(false);
  }
  
  @Deprecated
  public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly) { this(artifacts, excludes, latestOnly, false, false); }
  
  @Deprecated
  public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly, boolean allowEmptyArchive) { this(artifacts, excludes, latestOnly, allowEmptyArchive, false); }
  
  @Deprecated
  public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly, boolean allowEmptyArchive, boolean onlyIfSuccessful) { this(artifacts, excludes, latestOnly, allowEmptyArchive, onlyIfSuccessful, Boolean.valueOf(true)); }
  
  @Deprecated
  public ArtifactArchiver(String artifacts, String excludes, boolean latestOnly, boolean allowEmptyArchive, boolean onlyIfSuccessful, Boolean defaultExcludes) {
    this(artifacts);
    setExcludes(excludes);
    this.latestOnly = Boolean.valueOf(latestOnly);
    setAllowEmptyArchive(allowEmptyArchive);
    setOnlyIfSuccessful(onlyIfSuccessful);
    setDefaultExcludes(defaultExcludes.booleanValue());
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "Null checks in readResolve are valid since we deserialize and upgrade objects")
  protected Object readResolve() {
    if (this.allowEmptyArchive == null)
      this.allowEmptyArchive = Boolean.valueOf(SystemProperties.getBoolean(ArtifactArchiver.class.getName() + ".warnOnEmpty")); 
    if (this.defaultExcludes == null)
      this.defaultExcludes = Boolean.valueOf(true); 
    if (this.caseSensitive == null)
      this.caseSensitive = Boolean.valueOf(true); 
    if (this.followSymlinks == null)
      this.followSymlinks = Boolean.valueOf(true); 
    return this;
  }
  
  public String getArtifacts() { return this.artifacts; }
  
  @CheckForNull
  public String getExcludes() { return this.excludes; }
  
  @DataBoundSetter
  public final void setExcludes(@CheckForNull String excludes) { this.excludes = Util.fixEmptyAndTrim(excludes); }
  
  @Deprecated
  public boolean isLatestOnly() { return (this.latestOnly != null) ? this.latestOnly.booleanValue() : 0; }
  
  public boolean isOnlyIfSuccessful() { return this.onlyIfSuccessful; }
  
  @DataBoundSetter
  public final void setOnlyIfSuccessful(boolean onlyIfSuccessful) { this.onlyIfSuccessful = onlyIfSuccessful; }
  
  public boolean isFingerprint() { return this.fingerprint; }
  
  @DataBoundSetter
  public void setFingerprint(boolean fingerprint) { this.fingerprint = fingerprint; }
  
  public boolean getAllowEmptyArchive() { return this.allowEmptyArchive.booleanValue(); }
  
  @DataBoundSetter
  public final void setAllowEmptyArchive(boolean allowEmptyArchive) { this.allowEmptyArchive = Boolean.valueOf(allowEmptyArchive); }
  
  public boolean isDefaultExcludes() { return this.defaultExcludes.booleanValue(); }
  
  @DataBoundSetter
  public final void setDefaultExcludes(boolean defaultExcludes) { this.defaultExcludes = Boolean.valueOf(defaultExcludes); }
  
  public boolean isCaseSensitive() { return this.caseSensitive.booleanValue(); }
  
  @DataBoundSetter
  public final void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = Boolean.valueOf(caseSensitive); }
  
  public boolean isFollowSymlinks() { return this.followSymlinks.booleanValue(); }
  
  @DataBoundSetter
  public final void setFollowSymlinks(boolean followSymlinks) { this.followSymlinks = Boolean.valueOf(followSymlinks); }
  
  public void perform(Run<?, ?> build, FilePath ws, EnvVars environment, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
    if (this.artifacts.isEmpty())
      throw new AbortException(Messages.ArtifactArchiver_NoIncludes()); 
    Result result = build.getResult();
    if (this.onlyIfSuccessful && result != null && result.isWorseThan(Result.UNSTABLE)) {
      listener.getLogger().println(Messages.ArtifactArchiver_SkipBecauseOnlyIfSuccessful());
      return;
    } 
    listener.getLogger().println(Messages.ArtifactArchiver_ARCHIVING_ARTIFACTS());
    try {
      String artifacts = this.artifacts;
      if (build instanceof hudson.model.AbstractBuild)
        artifacts = environment.expand(artifacts); 
      Map<String, String> files = (Map)ws.act(new ListFiles(artifacts, this.excludes, this.defaultExcludes.booleanValue(), this.caseSensitive.booleanValue(), this.followSymlinks.booleanValue()));
      if (!files.isEmpty()) {
        build.pickArtifactManager().archive(ws, launcher, BuildListenerAdapter.wrap(listener), files);
        if (this.fingerprint) {
          Fingerprinter f = new Fingerprinter(artifacts);
          f.setExcludes(this.excludes);
          f.setDefaultExcludes(this.defaultExcludes.booleanValue());
          f.setCaseSensitive(this.caseSensitive.booleanValue());
          f.perform(build, ws, environment, launcher, listener);
        } 
      } else {
        result = build.getResult();
        if (result == null || result.isBetterOrEqualTo(Result.UNSTABLE)) {
          try {
            String msg = ws.validateAntFileMask(artifacts, FilePath.VALIDATE_ANT_FILE_MASK_BOUND, this.caseSensitive.booleanValue());
            if (msg != null)
              listener.getLogger().println(msg); 
          } catch (hudson.FilePath.FileMaskNoMatchesFoundException e) {
            listener.getLogger().println(e.getMessage());
          } catch (Exception e) {
            Functions.printStackTrace(e, listener.getLogger());
          } 
          if (this.allowEmptyArchive.booleanValue()) {
            listener.getLogger().println(Messages.ArtifactArchiver_NoMatchFound(artifacts));
          } else {
            throw new AbortException(Messages.ArtifactArchiver_NoMatchFound(artifacts));
          } 
        } 
      } 
    } catch (AccessDeniedException e) {
      LOG.log(Level.FINE, "Diagnosing anticipated Exception", e);
      throw new AbortException(e.toString());
    } 
  }
  
  public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }
}
