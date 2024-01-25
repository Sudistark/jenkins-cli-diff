package jenkins.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import jenkins.util.InterceptingExecutorService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextExecutorService extends InterceptingExecutorService {
  public SecurityContextExecutorService(ExecutorService service) { super(service); }
  
  protected Runnable wrap(Runnable r) {
    SecurityContext callingContext = SecurityContextHolder.getContext();
    return new Object(this, callingContext, r);
  }
  
  protected <V> Callable<V> wrap(Callable<V> c) {
    SecurityContext callingContext = SecurityContextHolder.getContext();
    return new Object(this, callingContext, c);
  }
}
