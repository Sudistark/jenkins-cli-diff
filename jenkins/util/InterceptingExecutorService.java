package jenkins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class InterceptingExecutorService implements ExecutorService {
  private final ExecutorService base;
  
  protected InterceptingExecutorService(ExecutorService base) { this.base = base; }
  
  protected abstract Runnable wrap(Runnable paramRunnable);
  
  protected abstract <V> Callable<V> wrap(Callable<V> paramCallable);
  
  protected ExecutorService delegate() { return this.base; }
  
  public <T> Future<T> submit(Callable<T> task) { return delegate().submit(wrap(task)); }
  
  public <T> Future<T> submit(Runnable task, T result) { return delegate().submit(wrap(task), result); }
  
  public Future<?> submit(Runnable task) { return delegate().submit(wrap(task)); }
  
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException { return delegate().invokeAll(wrap(tasks)); }
  
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException { return delegate().invokeAll(wrap(tasks), timeout, unit); }
  
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException { return (T)delegate().invokeAny(wrap(tasks)); }
  
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException { return (T)delegate().invokeAny(wrap(tasks), timeout, unit); }
  
  public void execute(Runnable command) { delegate().execute(wrap(command)); }
  
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return delegate().awaitTermination(timeout, unit); }
  
  public boolean isShutdown() { return delegate().isShutdown(); }
  
  public boolean isTerminated() { return delegate().isTerminated(); }
  
  public void shutdown() { delegate().shutdown(); }
  
  public List<Runnable> shutdownNow() { return delegate().shutdownNow(); }
  
  public String toString() { return delegate().toString(); }
  
  private <T> Collection<Callable<T>> wrap(Collection<? extends Callable<T>> callables) {
    List<Callable<T>> r = new ArrayList<Callable<T>>();
    for (Callable<T> c : callables)
      r.add(wrap(c)); 
    return r;
  }
}
