package org.acegisecurity.providers.dao;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public abstract class AbstractUserDetailsAuthenticationProvider implements AuthenticationProvider {
  private final AbstractUserDetailsAuthenticationProvider delegate = new Object(this);
  
  protected abstract void additionalAuthenticationChecks(UserDetails paramUserDetails, UsernamePasswordAuthenticationToken paramUsernamePasswordAuthenticationToken) throws AuthenticationException;
  
  protected abstract UserDetails retrieveUser(String paramString, UsernamePasswordAuthenticationToken paramUsernamePasswordAuthenticationToken) throws AuthenticationException;
  
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    try {
      return Authentication.fromSpring(this.delegate.authenticate(authentication.toSpring()));
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  public boolean supports(Class authentication) { return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication); }
}
