package org.acegisecurity.context;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.acegisecurity.Authentication;
import org.springframework.security.core.context.SecurityContext;

@Deprecated
public interface SecurityContext {
  Authentication getAuthentication();
  
  void setAuthentication(Authentication paramAuthentication);
  
  @NonNull
  static SecurityContext fromSpring(@NonNull SecurityContext c) { return new Object(c); }
  
  @NonNull
  default SecurityContext toSpring() { return new Object(this); }
}
