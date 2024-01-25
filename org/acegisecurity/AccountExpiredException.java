package org.acegisecurity;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public class AccountExpiredException extends AuthenticationException {
  public AccountExpiredException(String msg) { super(msg); }
  
  public AccountExpiredException(String msg, Throwable t) { super(msg, t); }
  
  public AccountExpiredException(String msg, Object extraInformation) { super(msg, extraInformation); }
  
  public AuthenticationException toSpring() { return new AccountExpiredException(toString(), this); }
  
  public static AccountExpiredException fromSpring(AccountExpiredException x) { return new AccountExpiredException(x.toString(), x); }
}
