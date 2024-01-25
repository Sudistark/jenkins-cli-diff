package org.acegisecurity;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class InsufficientAuthenticationException extends AuthenticationException {
  public InsufficientAuthenticationException(String msg) { super(msg); }
  
  public InsufficientAuthenticationException(String msg, Throwable t) { super(msg, t); }
  
  public AuthenticationException toSpring() { return new InsufficientAuthenticationException(toString(), this); }
  
  public static InsufficientAuthenticationException fromSpring(InsufficientAuthenticationException x) { return new InsufficientAuthenticationException(x.toString(), x); }
}
