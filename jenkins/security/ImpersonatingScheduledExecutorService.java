package jenkins.security;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import jenkins.util.InterceptingScheduledExecutorService;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;

public final class ImpersonatingScheduledExecutorService extends InterceptingScheduledExecutorService {
  private final Authentication authentication;
  
  public ImpersonatingScheduledExecutorService(ScheduledExecutorService base, Authentication authentication) {
    super(base);
    this.authentication = authentication;
  }
  
  @Deprecated
  public ImpersonatingScheduledExecutorService(ScheduledExecutorService base, Authentication authentication) { this(base, authentication.toSpring()); }
  
  protected Runnable wrap(Runnable r) { return new Object(this, r); }
  
  protected <V> Callable<V> wrap(Callable<V> r) { return new Object(this, r); }
}
