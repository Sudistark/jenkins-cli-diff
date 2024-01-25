package hudson.security;

import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UserDetailsServiceProxy implements UserDetailsService {
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserDetailsService uds = this.delegate;
    if (uds == null)
      throw new UserMayOrMayNotExistException2(Messages.UserDetailsServiceProxy_UnableToQuery(username)); 
    return uds.loadUserByUsername(username);
  }
  
  public void setDelegate(UserDetailsService core) { this.delegate = core; }
}
