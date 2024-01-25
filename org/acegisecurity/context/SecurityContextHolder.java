package org.acegisecurity.context;

import org.springframework.security.core.context.SecurityContextHolder;

@Deprecated
public class SecurityContextHolder {
  public static SecurityContext getContext() { return SecurityContext.fromSpring(SecurityContextHolder.getContext()); }
  
  public static void setContext(SecurityContext c) { SecurityContextHolder.setContext(c.toSpring()); }
  
  public static void clearContext() { SecurityContextHolder.clearContext(); }
}
