package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.acegisecurity.context.SecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ACLContext implements AutoCloseable {
  @NonNull
  private final SecurityContext previousContext;
  
  ACLContext(@NonNull SecurityContext previousContext) { this.previousContext = previousContext; }
  
  @NonNull
  public SecurityContext getPreviousContext2() { return this.previousContext; }
  
  @Deprecated
  public SecurityContext getPreviousContext() { return SecurityContext.fromSpring(getPreviousContext2()); }
  
  public void close() { SecurityContextHolder.setContext(this.previousContext); }
}
