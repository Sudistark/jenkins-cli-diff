package jenkins.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class InterceptingScheduledExecutorService extends InterceptingExecutorService implements ScheduledExecutorService {
  protected InterceptingScheduledExecutorService(ScheduledExecutorService base) { super(base); }
  
  protected ScheduledExecutorService delegate() { return (ScheduledExecutorService)super.delegate(); }
  
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) { return delegate().schedule(wrap(command), delay, unit); }
  
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) { return delegate().schedule(wrap(callable), delay, unit); }
  
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) { return delegate().scheduleAtFixedRate(wrap(command), initialDelay, period, unit); }
  
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) { return delegate().scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit); }
}
