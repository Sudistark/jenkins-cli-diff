package hudson.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserMayOrMayNotExistException2 extends UsernameNotFoundException {
  public UserMayOrMayNotExistException2(String msg) { super(msg); }
  
  public UserMayOrMayNotExistException2(String msg, Throwable t) { super(msg, t); }
}
