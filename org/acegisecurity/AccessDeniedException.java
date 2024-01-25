package org.acegisecurity;

import org.springframework.security.access.AccessDeniedException;

@Deprecated
public class AccessDeniedException extends AcegiSecurityException {
  public AccessDeniedException(String msg) { super(msg); }
  
  public AccessDeniedException(String msg, Throwable t) { super(msg, t); }
  
  public AccessDeniedException toSpring() { return new AccessDeniedException(toString(), this); }
}
