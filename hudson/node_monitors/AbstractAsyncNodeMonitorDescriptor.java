package hudson.node_monitors;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;
import hudson.model.Computer;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.slaves.SlaveComputer;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class AbstractAsyncNodeMonitorDescriptor<T> extends AbstractNodeMonitorDescriptor<T> {
  protected AbstractAsyncNodeMonitorDescriptor() {}
  
  protected AbstractAsyncNodeMonitorDescriptor(long interval) { super(interval); }
  
  protected AbstractAsyncNodeMonitorDescriptor(Class<? extends NodeMonitor> clazz) { super(clazz); }
  
  protected AbstractAsyncNodeMonitorDescriptor(Class<? extends NodeMonitor> clazz, long interval) { super(clazz, interval); }
  
  @CheckForNull
  protected abstract Callable<T, IOException> createCallable(Computer paramComputer);
  
  protected T monitor(Computer c) throws IOException, InterruptedException {
    VirtualChannel ch = c.getChannel();
    if (ch != null) {
      Callable<T, IOException> cc = createCallable(c);
      if (cc != null)
        return (T)ch.call(cc); 
    } 
    return null;
  }
  
  protected Map<Computer, T> monitor() throws InterruptedException { return monitorDetailed().getMonitoringData(); }
  
  @NonNull
  protected final Result<T> monitorDetailed() throws InterruptedException {
    Map<Computer, Future<T>> futures = new HashMap<Computer, Future<T>>();
    Set<Computer> skipped = new HashSet<Computer>();
    for (Computer c : Jenkins.get().getComputers()) {
      try {
        VirtualChannel ch = c.getChannel();
        futures.put(c, null);
        if (ch != null) {
          Callable<T, ?> cc = createCallable(c);
          if (cc != null)
            futures.put(c, ch.callAsync(cc)); 
        } 
      } catch (RuntimeException|IOException e) {
        error(c, e);
      } 
    } 
    long now = System.currentTimeMillis();
    long end = now + getMonitoringTimeOut();
    Map<Computer, T> data = new HashMap<Computer, T>();
    for (Map.Entry<Computer, Future<T>> e : futures.entrySet()) {
      Computer c = (Computer)e.getKey();
      Future<T> f = (Future)futures.get(c);
      data.put(c, null);
      if (f != null)
        try {
          data.put(c, f.get(Math.max(0L, end - System.currentTimeMillis()), TimeUnit.MILLISECONDS));
          continue;
        } catch (RuntimeException|java.util.concurrent.TimeoutException|java.util.concurrent.ExecutionException x) {
          error(c, x);
          continue;
        }  
      skipped.add(c);
    } 
    return new Result(data, skipped);
  }
  
  private void error(Computer c, Throwable x) {
    boolean cIsStillCurrent = (Jenkins.get().getComputer(c.getName()) == c);
    if (!cIsStillCurrent)
      return; 
    if (c instanceof SlaveComputer) {
      Functions.printStackTrace(x, ((SlaveComputer)c).getListener().error("Failed to monitor for " + getDisplayName()));
    } else {
      LOGGER.log(Level.WARNING, "Failed to monitor " + c.getDisplayName() + " for " + getDisplayName(), x);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(AbstractAsyncNodeMonitorDescriptor.class.getName());
}
