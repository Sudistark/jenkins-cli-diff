package org.acegisecurity.userdetails;

import hudson.security.UserMayOrMayNotExistException;
import hudson.security.UserMayOrMayNotExistException2;
import org.acegisecurity.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Deprecated
public class UsernameNotFoundException extends BadCredentialsException {
  public UsernameNotFoundException(String msg) { super(msg); }
  
  public UsernameNotFoundException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public UsernameNotFoundException(String msg, Throwable t) { super(msg, t); }
  
  public UsernameNotFoundException toSpring() { return new UsernameNotFoundException(toString(), this); }
  
  public static UsernameNotFoundException fromSpring(UsernameNotFoundException x) {
    if (x instanceof UserMayOrMayNotExistException2)
      return UserMayOrMayNotExistException.fromSpring((UserMayOrMayNotExistException2)x); 
    return new UsernameNotFoundException(x.toString(), x);
  }
}
