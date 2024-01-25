package hudson.model;

import hudson.Functions;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

public abstract class AsyncAperiodicWork extends AperiodicWork {
  private static final long LOG_ROTATE_MINUTES = SystemProperties.getLong(AsyncAperiodicWork.class.getName() + ".logRotateMinutes", 
      Long.valueOf(TimeUnit.DAYS.toMinutes(1L))).longValue();
  
  private static final long LOG_ROTATE_SIZE = SystemProperties.getLong(AsyncAperiodicWork.class.getName() + ".logRotateSize", 
      Long.valueOf(10485760L)).longValue();
  
  private final long logRotateMillis;
  
  private final long logRotateSize;
  
  private long lastRotateMillis;
  
  public final String name;
  
  private Thread thread;
  
  protected AsyncAperiodicWork(String name) {
    this.lastRotateMillis = Float.MIN_VALUE;
    this.name = name;
    this.logRotateMillis = TimeUnit.MINUTES.toMillis(
        SystemProperties.getLong(getClass().getName() + ".logRotateMinutes", Long.valueOf(LOG_ROTATE_MINUTES)).longValue());
    this.logRotateSize = SystemProperties.getLong(getClass().getName() + ".logRotateSize", Long.valueOf(LOG_ROTATE_SIZE)).longValue();
  }
  
  public final void doAperiodicRun() {
    try {
      if (this.thread != null && this.thread.isAlive()) {
        this.logger.log(getSlowLoggingLevel(), "{0} thread is still running. Execution aborted.", this.name);
        return;
      } 
      this.thread = new Thread(() -> {
            this.logger.log(Level.FINE, "Started {0}", this.name);
            startTime = System.currentTimeMillis();
            l = new AsyncPeriodicWork.LazyTaskListener(this::createListener, String.format("Started at %tc", new Object[] { new Date(startTime) }));
            try {
              ACLContext ctx = ACL.as2(ACL.SYSTEM2);
              try {
                execute(l);
                if (ctx != null)
                  ctx.close(); 
              } catch (Throwable throwable) {
                if (ctx != null)
                  try {
                    ctx.close();
                  } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                  }  
                throw throwable;
              } 
            } catch (IOException e) {
              Functions.printStackTrace(e, l.fatalError(e.getMessage()));
            } catch (InterruptedException e) {
              Functions.printStackTrace(e, l.fatalError("aborted"));
            } finally {
              stopTime = System.currentTimeMillis();
              l.close(String.format("Finished at %tc. %dms", new Object[] { new Date(stopTime), Long.valueOf(stopTime - startTime) }));
            } 
            this.logger.log(Level.FINE, "Finished {0}. {1,number} ms", new Object[] { this.name, 
                  Long.valueOf(stopTime - startTime) });
          }this.name + " thread");
      this.thread.start();
    } catch (Throwable t) {
      this.logger.log(Level.SEVERE, this.name + " thread failed with error", t);
    } 
  }
  
  protected StreamTaskListener createListener() {
    File f = getLogFile();
    if (!f.getParentFile().isDirectory() && 
      !f.getParentFile().mkdirs())
      this.logger.log(getErrorLoggingLevel(), "Could not create directory {0}", f.getParentFile()); 
    if (f.isFile()) {
      if (this.lastRotateMillis + this.logRotateMillis < System.currentTimeMillis() || (this.logRotateSize > 0L && f
        .length() > this.logRotateSize)) {
        this.lastRotateMillis = System.currentTimeMillis();
        File prev = null;
        for (int i = 5; i >= 0; i--) {
          File curr = (i == 0) ? f : new File(f.getParentFile(), f.getName() + "." + f.getName());
          if (curr.isFile())
            if (prev != null && !prev.exists()) {
              if (!curr.renameTo(prev))
                this.logger.log(getErrorLoggingLevel(), "Could not rotate log files {0} to {1}", new Object[] { curr, prev }); 
            } else if (!curr.delete()) {
              this.logger.log(getErrorLoggingLevel(), "Could not delete log file {0} to enable rotation", curr);
            }  
          prev = curr;
        } 
      } 
    } else {
      this.lastRotateMillis = System.currentTimeMillis();
      File oldFile = new File(Jenkins.get().getRootDir(), f.getName());
      if (oldFile.isFile()) {
        File newFile = new File(f.getParentFile(), f.getName() + ".1");
        if (!newFile.isFile())
          if (oldFile.renameTo(newFile)) {
            this.logger.log(getNormalLoggingLevel(), "Moved {0} to {1}", new Object[] { oldFile, newFile });
          } else {
            this.logger.log(getErrorLoggingLevel(), "Could not move {0} to {1}", new Object[] { oldFile, newFile });
          }  
      } 
    } 
    try {
      return new StreamTaskListener(f, true, null);
    } catch (IOException e) {
      throw new Error(e);
    } 
  }
  
  protected File getLogFile() {
    return new File(getLogsRoot(), "/tasks/" + this.name + ".log");
  }
  
  protected Level getNormalLoggingLevel() { return Level.INFO; }
  
  protected Level getSlowLoggingLevel() { return getNormalLoggingLevel(); }
  
  protected Level getErrorLoggingLevel() { return Level.SEVERE; }
  
  protected abstract void execute(TaskListener paramTaskListener) throws IOException, InterruptedException;
}
