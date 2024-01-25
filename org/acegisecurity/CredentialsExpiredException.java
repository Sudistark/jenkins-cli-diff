package org.acegisecurity;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class CredentialsExpiredException extends AuthenticationException {
  public CredentialsExpiredException(String msg) { super(msg); }
  
  public CredentialsExpiredException(String msg, Throwable t) { super(msg, t); }
  
  public CredentialsExpiredException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public AuthenticationException toSpring() { return new CredentialsExpiredException(toString(), this); }
  
  public static CredentialsExpiredException fromSpring(CredentialsExpiredException x) { return new CredentialsExpiredException(x.toString(), x); }
}
