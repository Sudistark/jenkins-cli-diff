package org.acegisecurity.providers;

import org.acegisecurity.AuthenticationException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class ProviderNotFoundException extends AuthenticationException {
  public ProviderNotFoundException(String msg) { super(msg); }
  
  public ProviderNotFoundException(String msg, Throwable t) { super(msg, t); }
  
  public AuthenticationException toSpring() { return (AuthenticationException)(new ProviderNotFoundException(toString())).initCause(this); }
  
  public static ProviderNotFoundException fromSpring(ProviderNotFoundException x) { return new ProviderNotFoundException(x.toString(), x); }
}
