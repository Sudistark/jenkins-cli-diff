package org.acegisecurity;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class DisabledException extends AuthenticationException {
  public DisabledException(String msg) { super(msg); }
  
  public DisabledException(String msg, Throwable t) { super(msg, t); }
  
  public DisabledException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public AuthenticationException toSpring() { return new DisabledException(toString(), this); }
  
  public static DisabledException fromSpring(DisabledException x) { return new DisabledException(x.toString(), x); }
}
