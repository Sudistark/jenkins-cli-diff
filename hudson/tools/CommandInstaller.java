package hudson.tools;

import hudson.FilePath;
import hudson.util.LineEndingConversion;
import java.io.ObjectStreamException;
import org.kohsuke.stapler.DataBoundConstructor;

public class CommandInstaller extends AbstractCommandInstaller {
  @DataBoundConstructor
  public CommandInstaller(String label, String command, String toolHome) { super(label, LineEndingConversion.convertEOL(command, LineEndingConversion.EOLType.Unix), toolHome); }
  
  public String getCommandFileExtension() { return ".sh"; }
  
  public String[] getCommandCall(FilePath script) { return new String[] { "sh", "-e", script.getRemote() }; }
  
  private Object readResolve() throws ObjectStreamException { return new CommandInstaller(getLabel(), getCommand(), getToolHome()); }
}
