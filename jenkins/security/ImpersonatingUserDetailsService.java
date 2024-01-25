package jenkins.security;

import hudson.model.User;
import hudson.security.UserMayOrMayNotExistException;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataAccessException;

@Deprecated
public class ImpersonatingUserDetailsService implements UserDetailsService {
  private final UserDetailsService base;
  
  public ImpersonatingUserDetailsService(UserDetailsService base) { this.base = base; }
  
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    try {
      return this.base.loadUserByUsername(username);
    } catch (UserMayOrMayNotExistException|DataAccessException e) {
      return attemptToImpersonate(username, e);
    } 
  }
  
  protected UserDetails attemptToImpersonate(String username, RuntimeException e) {
    User u = User.getById(username, false);
    if (u != null) {
      LastGrantedAuthoritiesProperty p = (LastGrantedAuthoritiesProperty)u.getProperty(LastGrantedAuthoritiesProperty.class);
      if (p != null)
        return new User(username, "", true, true, true, true, p
            .getAuthorities()); 
    } 
    throw e;
  }
}
