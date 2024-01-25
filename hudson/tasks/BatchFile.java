package hudson.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.FilePath;
import hudson.util.LineEndingConversion;
import java.util.ArrayList;
import java.util.List;
import jenkins.tasks.filters.EnvVarsFilterLocalRule;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class BatchFile extends CommandInterpreter {
  private Integer unstableReturn;
  
  @DataBoundConstructor
  public BatchFile(String command) { super(LineEndingConversion.convertEOL(command, LineEndingConversion.EOLType.Windows)); }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  @DataBoundSetter
  public void setConfiguredLocalRules(List<EnvVarsFilterLocalRule> configuredLocalRules) { this.configuredLocalRules = configuredLocalRules; }
  
  public String[] buildCommandLine(FilePath script) { return new String[] { "cmd", "/c", "call", script.getRemote() }; }
  
  protected String getContents() { return LineEndingConversion.convertEOL(this.command + "\r\nexit %ERRORLEVEL%", LineEndingConversion.EOLType.Windows); }
  
  protected String getFileExtension() { return ".bat"; }
  
  @CheckForNull
  public final Integer getUnstableReturn() { return Integer.valueOf(0).equals(this.unstableReturn) ? null : this.unstableReturn; }
  
  @DataBoundSetter
  public void setUnstableReturn(Integer unstableReturn) { this.unstableReturn = unstableReturn; }
  
  protected boolean isErrorlevelForUnstableBuild(int exitCode) { return (this.unstableReturn != null && exitCode != 0 && this.unstableReturn.equals(Integer.valueOf(exitCode))); }
  
  private Object readResolve() {
    BatchFile batch = new BatchFile(this.command);
    batch.setUnstableReturn(this.unstableReturn);
    batch.setConfiguredLocalRules((this.configuredLocalRules == null) ? new ArrayList() : this.configuredLocalRules);
    return batch;
  }
}
