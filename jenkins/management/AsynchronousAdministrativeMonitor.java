package jenkins.management;

import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.AdministrativeMonitor;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class AsynchronousAdministrativeMonitor extends AdministrativeMonitor {
  public boolean isFixingActive() { return (this.fixThread != null && this.fixThread.isAlive()); }
  
  public AnnotatedLargeText getLogText() {
    return new AnnotatedLargeText(
        getLogFile(), Charset.defaultCharset(), 
        !isFixingActive(), this);
  }
  
  protected File getLogFile() {
    File base = getBaseDir();
    try {
      Util.createDirectories(base.toPath(), new java.nio.file.attribute.FileAttribute[0]);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } 
    return new File(base, "log");
  }
  
  protected File getBaseDir() { return new File(Jenkins.get().getRootDir(), getClass().getName()); }
  
  public abstract String getDisplayName();
  
  protected Thread start(boolean forceRestart) {
    if (!forceRestart && isFixingActive())
      this.fixThread.interrupt(); 
    if (forceRestart || !isFixingActive()) {
      this.fixThread = new FixThread(this);
      this.fixThread.start();
    } 
    return this.fixThread;
  }
  
  private static final Logger LOGGER = Logger.getLogger(AsynchronousAdministrativeMonitor.class.getName());
  
  protected abstract void fix(TaskListener paramTaskListener) throws Exception;
}
