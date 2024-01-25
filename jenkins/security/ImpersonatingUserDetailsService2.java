package jenkins.security;

import hudson.model.User;
import hudson.security.UserMayOrMayNotExistException2;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class ImpersonatingUserDetailsService2 implements UserDetailsService {
  private final UserDetailsService base;
  
  public ImpersonatingUserDetailsService2(UserDetailsService base) { this.base = base; }
  
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      return this.base.loadUserByUsername(username);
    } catch (UserMayOrMayNotExistException2 e) {
      return attemptToImpersonate(username, e);
    } 
  }
  
  protected UserDetails attemptToImpersonate(String username, RuntimeException e) {
    User u = User.getById(username, false);
    if (u != null) {
      LastGrantedAuthoritiesProperty p = (LastGrantedAuthoritiesProperty)u.getProperty(LastGrantedAuthoritiesProperty.class);
      if (p != null)
        return new User(username, "", true, true, true, true, p.getAuthorities2()); 
    } 
    throw e;
  }
}
