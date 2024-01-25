package hudson.util;

import hudson.Extension;
import hudson.Util;
import hudson.model.AdministrativeMonitor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@Extension
public class DoubleLaunchChecker extends AdministrativeMonitor {
  private long lastWriteTime = 0L;
  
  private boolean activated;
  
  public final File home = Jenkins.get().getRootDir();
  
  private String collidingId;
  
  public String getDisplayName() { return Messages.DoubleLaunchChecker_duplicate_jenkins_checker(); }
  
  public boolean isActivated() { return this.activated; }
  
  protected void execute() {
    LOGGER.fine("running detector");
    File timestampFile = new File(this.home, ".owner");
    long t = timestampFile.lastModified();
    if (t != 0L && this.lastWriteTime != 0L && t != this.lastWriteTime && isEnabled()) {
      try {
        this.collidingId = Files.readString(Util.fileToPath(timestampFile), Charset.defaultCharset());
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to read collision file", e);
      } 
      this.activated = true;
      LOGGER.severe("Collision detected. timestamp=" + t + ", expected=" + this.lastWriteTime);
    } 
    try {
      Files.writeString(Util.fileToPath(timestampFile), getId(), Charset.defaultCharset(), new java.nio.file.OpenOption[0]);
      this.lastWriteTime = timestampFile.lastModified();
    } catch (IOException e) {
      LOGGER.log(Level.FINE, null, e);
      this.lastWriteTime = 0L;
    } 
  }
  
  public String getId() { return Long.toString(ProcessHandle.current().pid()); }
  
  public String getCollidingId() { return this.collidingId; }
  
  private static final Logger LOGGER = Logger.getLogger(DoubleLaunchChecker.class.getName());
}
