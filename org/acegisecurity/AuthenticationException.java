package org.acegisecurity;

import org.acegisecurity.providers.ProviderNotFoundException;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Deprecated
public abstract class AuthenticationException extends AcegiSecurityException {
  private Authentication authentication;
  
  private Object extraInformation;
  
  protected AuthenticationException(String msg) { super(msg); }
  
  protected AuthenticationException(String msg, Object extraInformation) {
    super(msg);
    this.extraInformation = extraInformation;
  }
  
  protected AuthenticationException(String msg, Throwable t) { super(msg, t); }
  
  public Authentication getAuthentication() { return this.authentication; }
  
  public void setAuthentication(Authentication authentication) { this.authentication = authentication; }
  
  public Object getExtraInformation() { return this.extraInformation; }
  
  public void clearExtraInformation() { this.extraInformation = null; }
  
  public AuthenticationException toSpring() { return new Object(this, toString(), this); }
  
  public static RuntimeException fromSpring(AuthenticationException x) {
    if (x instanceof BadCredentialsException)
      return BadCredentialsException.fromSpring((BadCredentialsException)x); 
    if (x instanceof AuthenticationServiceException)
      return AuthenticationServiceException.fromSpring((AuthenticationServiceException)x); 
    if (x instanceof AccountExpiredException)
      return AccountExpiredException.fromSpring((AccountExpiredException)x); 
    if (x instanceof CredentialsExpiredException)
      return CredentialsExpiredException.fromSpring((CredentialsExpiredException)x); 
    if (x instanceof DisabledException)
      return DisabledException.fromSpring((DisabledException)x); 
    if (x instanceof InsufficientAuthenticationException)
      return InsufficientAuthenticationException.fromSpring((InsufficientAuthenticationException)x); 
    if (x instanceof LockedException)
      return LockedException.fromSpring((LockedException)x); 
    if (x instanceof ProviderNotFoundException)
      return ProviderNotFoundException.fromSpring((ProviderNotFoundException)x); 
    if (x instanceof hudson.security.UserMayOrMayNotExistException2 && x.getCause() instanceof DataAccessException)
      return (DataAccessException)x.getCause(); 
    if (x instanceof UsernameNotFoundException)
      return UsernameNotFoundException.fromSpring((UsernameNotFoundException)x); 
    return new Object(x.toString(), x);
  }
}
