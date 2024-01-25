package hudson.security;

import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Deprecated
public class UserMayOrMayNotExistException extends UsernameNotFoundException {
  public UserMayOrMayNotExistException(String msg) { super(msg); }
  
  public UserMayOrMayNotExistException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public UserMayOrMayNotExistException(String msg, Throwable t) { super(msg, t); }
  
  public UserMayOrMayNotExistException2 toSpring() { return new UserMayOrMayNotExistException2(toString(), this); }
  
  public static UserMayOrMayNotExistException fromSpring(UserMayOrMayNotExistException2 x) { return new UserMayOrMayNotExistException(x.toString(), x); }
}
