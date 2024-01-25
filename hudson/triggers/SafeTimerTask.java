package hudson.triggers;

import hudson.security.ACL;
import hudson.security.ACLContext;
import java.io.File;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

public abstract class SafeTimerTask extends TimerTask {
  public static SafeTimerTask of(ExceptionRunnable r) { return new Object(r); }
  
  static final String LOGS_ROOT_PATH_PROPERTY = SafeTimerTask.class.getName() + ".logsTargetDir";
  
  private static boolean ALREADY_LOGGED = false;
  
  public final void run() {
    try {
      ACLContext ctx = ACL.as2(ACL.SYSTEM2);
      try {
        doRun();
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
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE, "Timer task " + this + " failed", t);
    } 
  }
  
  protected abstract void doRun();
  
  public static File getLogsRoot() {
    tagsLogsPath = SystemProperties.getString(LOGS_ROOT_PATH_PROPERTY);
    if (tagsLogsPath == null)
      return new File(Jenkins.get().getRootDir(), "logs"); 
    Level logLevel = Level.INFO;
    if (ALREADY_LOGGED)
      logLevel = Level.FINE; 
    LOGGER.log(logLevel, "Using non default root path for tasks logging: {0}. (Beware: no automated migration if you change or remove it again)", LOGS_ROOT_PATH_PROPERTY);
    ALREADY_LOGGED = true;
    return new File(tagsLogsPath);
  }
  
  private static final Logger LOGGER = Logger.getLogger(SafeTimerTask.class.getName());
}
