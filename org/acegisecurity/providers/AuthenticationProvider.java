package org.acegisecurity.providers;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;

@Deprecated
public interface AuthenticationProvider {
  Authentication authenticate(Authentication paramAuthentication) throws AuthenticationException;
  
  boolean supports(Class paramClass);
}
