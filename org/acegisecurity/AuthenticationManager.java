package org.acegisecurity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Deprecated
public interface AuthenticationManager {
  static AuthenticationManager fromSpring(AuthenticationManager am) {
    return authentication -> {
        try {
          return Authentication.fromSpring(am.authenticate(authentication.toSpring()));
        } catch (AuthenticationException x) {
          throw AuthenticationException.fromSpring(x);
        } 
      };
  }
  
  default AuthenticationManager toSpring() {
    return authentication -> {
        try {
          return authenticate(Authentication.fromSpring(authentication)).toSpring();
        } catch (AcegiSecurityException x) {
          throw x.toSpring();
        } 
      };
  }
  
  Authentication authenticate(Authentication paramAuthentication) throws AuthenticationException;
}
