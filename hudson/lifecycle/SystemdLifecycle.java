package hudson.lifecycle;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension(optional = true)
public class SystemdLifecycle extends ExitLifecycle {
  private static final Logger LOGGER = Logger.getLogger(SystemdLifecycle.class.getName());
  
  public void onReady() {
    super.onReady();
    notify("READY=1");
  }
  
  public void onReload(@NonNull String user, @CheckForNull String remoteAddr) {
    super.onReload(user, remoteAddr);
    notify("RELOADING=1");
  }
  
  public void onStop(@NonNull String user, @CheckForNull String remoteAddr) {
    super.onStop(user, remoteAddr);
    notify("STOPPING=1");
  }
  
  public void onExtendTimeout(long timeout, @NonNull TimeUnit unit) {
    super.onExtendTimeout(timeout, unit);
    notify(String.format("EXTEND_TIMEOUT_USEC=%d", new Object[] { Long.valueOf(unit.toMicros(timeout)) }));
  }
  
  public void onStatusUpdate(String status) {
    super.onStatusUpdate(status);
    notify(String.format("STATUS=%s", new Object[] { status }));
  }
  
  private static void notify(String message) {
    int rv = Systemd.INSTANCE.sd_notify(0, message);
    if (rv < 0)
      LOGGER.log(Level.WARNING, "sd_notify(3) returned {0}", Integer.valueOf(rv)); 
  }
}
