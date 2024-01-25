package hudson.security;

import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AuthenticationManagerProxy implements AuthenticationManager {
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    AuthenticationManager m = this.delegate;
    if (m == null)
      throw new DisabledException("Authentication service is still not ready yet"); 
    return m.authenticate(authentication);
  }
  
  public void setDelegate(AuthenticationManager manager) { this.delegate = manager; }
}
