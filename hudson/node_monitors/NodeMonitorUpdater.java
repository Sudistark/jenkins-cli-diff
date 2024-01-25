package hudson.node_monitors;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.util.Futures;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import jenkins.util.Timer;

@Extension
public class NodeMonitorUpdater extends ComputerListener {
  private static final Runnable MONITOR_UPDATER = new Object();
  
  private Future<?> future = Futures.precomputed(null);
  
  public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
    synchronized (this) {
      this.future.cancel(false);
      this.future = Timer.get().schedule(MONITOR_UPDATER, 5L, TimeUnit.SECONDS);
    } 
  }
}
