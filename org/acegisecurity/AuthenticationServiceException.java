package org.acegisecurity;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class AuthenticationServiceException extends AuthenticationException {
  public AuthenticationServiceException(String msg) { super(msg); }
  
  public AuthenticationServiceException(String msg, Throwable t) { super(msg, t); }
  
  public AuthenticationException toSpring() { return new AuthenticationServiceException(toString(), this); }
  
  public static AuthenticationServiceException fromSpring(AuthenticationServiceException x) { return new AuthenticationServiceException(x.toString(), x); }
}
