package jenkins.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.ACL;
import hudson.util.ClassLoaderSanityThreadFactory;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import jenkins.security.ImpersonatingScheduledExecutorService;

public class Timer {
  static ScheduledExecutorService executorService;
  
  @NonNull
  public static ScheduledExecutorService get() {
    if (executorService == null)
      executorService = new ImpersonatingScheduledExecutorService(new ErrorLoggingScheduledThreadPoolExecutor(10, new NamingThreadFactory(new ClassLoaderSanityThreadFactory(new DaemonThreadFactory()), "jenkins.util.Timer")), ACL.SYSTEM2); 
    return executorService;
  }
  
  public static void shutdown() {
    if (executorService != null) {
      executorService.shutdownNow();
      executorService = null;
    } 
  }
}
