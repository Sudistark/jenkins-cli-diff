package org.acegisecurity;

import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Deprecated
public class BadCredentialsException extends AuthenticationException {
  public BadCredentialsException(String msg) { super(msg); }
  
  public BadCredentialsException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public BadCredentialsException(String msg, Throwable t) { super(msg, t); }
  
  public AuthenticationException toSpring() { return new BadCredentialsException(toString(), this); }
  
  public static BadCredentialsException fromSpring(AuthenticationException x) {
    if (x instanceof UsernameNotFoundException)
      return UsernameNotFoundException.fromSpring((UsernameNotFoundException)x); 
    return new BadCredentialsException(x.toString(), x);
  }
}
