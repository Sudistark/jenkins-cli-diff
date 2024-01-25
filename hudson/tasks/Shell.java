package hudson.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.util.LineEndingConversion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import jenkins.tasks.filters.EnvVarsFilterLocalRule;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class Shell extends CommandInterpreter {
  private Integer unstableReturn;
  
  @DataBoundConstructor
  public Shell(String command) { super(LineEndingConversion.convertEOL(command, LineEndingConversion.EOLType.Unix)); }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  @DataBoundSetter
  public void setConfiguredLocalRules(List<EnvVarsFilterLocalRule> configuredLocalRules) { this.configuredLocalRules = configuredLocalRules; }
  
  private static String addLineFeedForNonASCII(String s) {
    if (!s.startsWith("#!") && 
      s.indexOf('\n') != 0)
      return "\n" + s; 
    return s;
  }
  
  public String[] buildCommandLine(FilePath script) {
    if (this.command.startsWith("#!")) {
      int end = this.command.indexOf('\n');
      if (end < 0)
        end = this.command.length(); 
      List<String> args = new ArrayList<String>(Arrays.asList(Util.tokenize(this.command.substring(0, end).trim())));
      args.add(script.getRemote());
      args.set(0, ((String)args.get(0)).substring(2));
      return (String[])args.toArray(new String[0]);
    } 
    return new String[] { getDescriptor().getShellOrDefault(script.getChannel()), "-xe", script.getRemote() };
  }
  
  protected String getContents() { return addLineFeedForNonASCII(LineEndingConversion.convertEOL(this.command, LineEndingConversion.EOLType.Unix)); }
  
  protected String getFileExtension() { return ".sh"; }
  
  @CheckForNull
  public final Integer getUnstableReturn() { return Integer.valueOf(0).equals(this.unstableReturn) ? null : this.unstableReturn; }
  
  @DataBoundSetter
  public void setUnstableReturn(Integer unstableReturn) { this.unstableReturn = unstableReturn; }
  
  protected boolean isErrorlevelForUnstableBuild(int exitCode) { return (this.unstableReturn != null && exitCode != 0 && this.unstableReturn.equals(Integer.valueOf(exitCode))); }
  
  public DescriptorImpl getDescriptor() { return (DescriptorImpl)super.getDescriptor(); }
  
  private Object readResolve() {
    Shell shell = new Shell(this.command);
    shell.setUnstableReturn(this.unstableReturn);
    shell.setConfiguredLocalRules((this.configuredLocalRules == null) ? new ArrayList() : this.configuredLocalRules);
    return shell;
  }
  
  private static final Logger LOGGER = Logger.getLogger(Shell.class.getName());
}
