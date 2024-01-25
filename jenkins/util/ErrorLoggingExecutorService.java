package jenkins.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorLoggingExecutorService extends InterceptingExecutorService {
  private static final Logger LOGGER = Logger.getLogger(ErrorLoggingExecutorService.class.getName());
  
  public ErrorLoggingExecutorService(ExecutorService base) { super(base); }
  
  protected Runnable wrap(Runnable r) {
    return () -> {
        try {
          r.run();
        } catch (Throwable x) {
          LOGGER.log(Level.WARNING, null, x);
          throw x;
        } 
      };
  }
  
  protected <V> Callable<V> wrap(Callable<V> r) { return r; }
}
