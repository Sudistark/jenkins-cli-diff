package jenkins.slaves.restarter;

import hudson.Extension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

@Extension
public class WinswSlaveRestarter extends SlaveRestarter {
  private String exe;
  
  public boolean canWork() {
    try {
      this.exe = System.getenv("WINSW_EXECUTABLE");
      if (this.exe == null)
        return false; 
      return (exec("status") == 0);
    } catch (InterruptedException|IOException e) {
      LOGGER.log(Level.FINE, "" + getClass() + " unsuitable", e);
      return false;
    } 
  }
  
  private int exec(String cmd) throws InterruptedException, IOException {
    ProcessBuilder pb = new ProcessBuilder(new String[] { this.exe, cmd });
    pb.redirectErrorStream(true);
    Process p = pb.start();
    p.getOutputStream().close();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.copy(p.getInputStream(), baos);
    int r = p.waitFor();
    if (r != 0)
      LOGGER.info(this.exe + " cmd: output:\n" + this.exe); 
    return r;
  }
  
  public void restart() {
    int r = exec("restart!");
    throw new IOException("Restart failure. '" + this.exe + " restart' completed with " + r + " but I'm still alive!  See https://www.jenkins.io/redirect/troubleshooting/windows-agent-restart for a possible explanation and solution");
  }
  
  private static final Logger LOGGER = Logger.getLogger(WinswSlaveRestarter.class.getName());
  
  private static final long serialVersionUID = 1L;
}
