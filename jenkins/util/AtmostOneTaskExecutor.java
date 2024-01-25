package jenkins.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.remoting.AtmostOneThreadExecutor;
import hudson.security.ACL;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import jenkins.security.ImpersonatingExecutorService;

public class AtmostOneTaskExecutor<V> extends Object {
  private static final Logger LOGGER = Logger.getLogger(AtmostOneTaskExecutor.class.getName());
  
  private final ExecutorService base;
  
  private final Callable<V> task;
  
  private CompletableFuture<V> pending;
  
  private CompletableFuture<V> inprogress;
  
  public AtmostOneTaskExecutor(ExecutorService base, Callable<V> task) {
    this.base = base;
    this.task = task;
  }
  
  public AtmostOneTaskExecutor(Callable<V> task) {
    this(new ImpersonatingExecutorService(new AtmostOneThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), 
              
              String.format("AtmostOneTaskExecutor[%s]", new Object[] { task }))), ACL.SYSTEM2), task);
  }
  
  public Future<V> submit() {
    if (this.pending == null) {
      this.pending = new CompletableFuture();
      maybeRun();
    } 
    return this.pending;
  }
  
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED_BAD_PRACTICE"}, justification = "method signature does not permit plumbing through the return value")
  private void maybeRun() {
    if (this.inprogress == null && this.pending != null)
      this.base.submit(new Object(this)); 
  }
}
