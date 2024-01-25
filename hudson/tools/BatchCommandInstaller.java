package hudson.tools;

import hudson.FilePath;
import hudson.util.LineEndingConversion;
import java.io.ObjectStreamException;
import org.kohsuke.stapler.DataBoundConstructor;

public class BatchCommandInstaller extends AbstractCommandInstaller {
  @DataBoundConstructor
  public BatchCommandInstaller(String label, String command, String toolHome) { super(label, LineEndingConversion.convertEOL(command, LineEndingConversion.EOLType.Windows), toolHome); }
  
  public String getCommandFileExtension() { return ".bat"; }
  
  public String[] getCommandCall(FilePath script) { return new String[] { "cmd", "/c", "call", script.getRemote() }; }
  
  private Object readResolve() throws ObjectStreamException { return new BatchCommandInstaller(getLabel(), getCommand(), getToolHome()); }
}
