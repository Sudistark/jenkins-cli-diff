package org.acegisecurity;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class LockedException extends AuthenticationException {
  public LockedException(String msg) { super(msg); }
  
  public LockedException(String msg, Throwable t) { super(msg, t); }
  
  public LockedException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public AuthenticationException toSpring() { return new LockedException(toString(), this); }
  
  public static LockedException fromSpring(LockedException x) { return new LockedException(x.toString(), x); }
}
