package jenkins.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import jenkins.util.InterceptingExecutorService;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;

public final class ImpersonatingExecutorService extends InterceptingExecutorService {
  private final Authentication authentication;
  
  public ImpersonatingExecutorService(ExecutorService base, Authentication authentication) {
    super(base);
    this.authentication = authentication;
  }
  
  @Deprecated
  public ImpersonatingExecutorService(ExecutorService base, Authentication authentication) { this(base, authentication.toSpring()); }
  
  protected Runnable wrap(Runnable r) { return new Object(this, r); }
  
  protected <V> Callable<V> wrap(Callable<V> r) { return new Object(this, r); }
}
