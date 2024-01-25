package jenkins.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class ContextResettingExecutorService extends InterceptingExecutorService {
  public ContextResettingExecutorService(ExecutorService base) { super(base); }
  
  protected Runnable wrap(Runnable r) { return new Object(this, r); }
  
  protected <V> Callable<V> wrap(Callable<V> r) { return new Object(this, r); }
}
