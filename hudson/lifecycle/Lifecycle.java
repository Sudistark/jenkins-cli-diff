package hudson.lifecycle;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.apache.commons.io.FileUtils;

public abstract class Lifecycle implements ExtensionPoint {
  private static Lifecycle INSTANCE = null;
  
  public static Lifecycle get() {
    if (INSTANCE == null) {
      Object object;
      String p = SystemProperties.getString("hudson.lifecycle");
      if (p != null) {
        try {
          ClassLoader cl = (Jenkins.get().getPluginManager()).uberClassLoader;
          object = (Lifecycle)cl.loadClass(p).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (NoSuchMethodException e) {
          NoSuchMethodError x = new NoSuchMethodError(e.getMessage());
          x.initCause(e);
          throw x;
        } catch (InstantiationException e) {
          InstantiationError x = new InstantiationError(e.getMessage());
          x.initCause(e);
          throw x;
        } catch (IllegalAccessException e) {
          IllegalAccessError x = new IllegalAccessError(e.getMessage());
          x.initCause(e);
          throw x;
        } catch (ClassNotFoundException e) {
          NoClassDefFoundError x = new NoClassDefFoundError(e.getMessage());
          x.initCause(e);
          throw x;
        } catch (InvocationTargetException e) {
          Throwable t = e.getCause();
          if (t instanceof RuntimeException)
            throw (RuntimeException)t; 
          if (t instanceof IOException)
            throw new UncheckedIOException((IOException)t); 
          if (t instanceof Exception)
            throw new RuntimeException(t); 
          if (t instanceof Error)
            throw (Error)t; 
          throw new Error(e);
        } 
      } else if (Functions.isWindows()) {
        object = new Object();
      } else if (System.getenv("SMF_FMRI") != null && System.getenv("SMF_RESTARTER") != null) {
        object = new SolarisSMFLifecycle();
      } else if (System.getenv("NOTIFY_SOCKET") != null) {
        object = new SystemdLifecycle();
      } else {
        try {
          object = new UnixLifecycle();
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Failed to install embedded lifecycle implementation", e);
          object = new Object(e);
        } 
      } 
      assert object != null;
      INSTANCE = object;
    } 
    return INSTANCE;
  }
  
  public File getHudsonWar() {
    String war = SystemProperties.getString("executable-war");
    if (war != null && (new File(war)).exists())
      return new File(war); 
    return null;
  }
  
  public void rewriteHudsonWar(File by) throws IOException {
    File dest = getHudsonWar();
    if (dest == null)
      throw new IOException("jenkins.war location is not known."); 
    File bak = new File(dest.getPath() + ".bak");
    if (!by.equals(bak))
      FileUtils.copyFile(dest, bak); 
    FileUtils.copyFile(by, dest);
    if (by.equals(bak))
      Files.deleteIfExists(Util.fileToPath(bak)); 
  }
  
  public boolean canRewriteHudsonWar() {
    File f = getHudsonWar();
    if (f == null || !f.canWrite())
      return false; 
    File parent = f.getParentFile();
    if (parent == null || !parent.canWrite())
      return false; 
    return true;
  }
  
  public void restart() { throw new UnsupportedOperationException(); }
  
  public void verifyRestartable() {
    if (!Util.isOverridden(Lifecycle.class, getClass(), "restart", new Class[0]))
      throw new RestartNotSupportedException("Restart is not supported in this running mode (" + 
          getClass().getName() + ")."); 
  }
  
  public boolean canRestart() {
    try {
      verifyRestartable();
      return true;
    } catch (RestartNotSupportedException e) {
      return false;
    } 
  }
  
  public void onReady() { LOGGER.log(Level.INFO, "Jenkins is fully up and running"); }
  
  public void onReload(@NonNull String user, @CheckForNull String remoteAddr) {
    if (remoteAddr != null) {
      LOGGER.log(Level.INFO, "Reloading Jenkins as requested by {0} from {1}", new Object[] { user, remoteAddr });
    } else {
      LOGGER.log(Level.INFO, "Reloading Jenkins as requested by {0}", user);
    } 
  }
  
  public void onStop(@NonNull String user, @CheckForNull String remoteAddr) {
    if (remoteAddr != null) {
      LOGGER.log(Level.INFO, "Stopping Jenkins as requested by {0} from {1}", new Object[] { user, remoteAddr });
    } else {
      LOGGER.log(Level.INFO, "Stopping Jenkins as requested by {0}", user);
    } 
  }
  
  public void onExtendTimeout(long timeout, @NonNull TimeUnit unit) {}
  
  public void onStatusUpdate(String status) { LOGGER.log(Level.INFO, status); }
  
  private static final Logger LOGGER = Logger.getLogger(Lifecycle.class.getName());
}
