package hudson.tools;

import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;

public abstract class AbstractCommandInstaller extends ToolInstaller {
  private final String command;
  
  private final String toolHome;
  
  protected AbstractCommandInstaller(String label, String command, String toolHome) {
    super(label);
    this.command = command;
    this.toolHome = toolHome;
  }
  
  public String getCommand() { return this.command; }
  
  public String getToolHome() { return this.toolHome; }
  
  public abstract String getCommandFileExtension();
  
  public abstract String[] getCommandCall(FilePath paramFilePath);
  
  public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
    FilePath dir = preferredLocation(tool, node);
    script = dir.createTextTempFile("hudson", getCommandFileExtension(), this.command);
    try {
      String[] cmd = getCommandCall(script);
      int r = node.createLauncher(log).launch().cmds(cmd).stdout(log).pwd(dir).join();
      if (r != 0)
        throw new IOException("Command returned status " + r); 
    } finally {
      script.delete();
    } 
    return dir.child(getToolHome());
  }
}
