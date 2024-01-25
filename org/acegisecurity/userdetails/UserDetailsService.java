package org.acegisecurity.userdetails;

import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.AuthenticationException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Deprecated
public interface UserDetailsService {
  static UserDetailsService fromSpring(UserDetailsService uds) {
    return username -> {
        try {
          return UserDetails.fromSpring(uds.loadUserByUsername(username));
        } catch (AuthenticationException x) {
          throw AuthenticationException.fromSpring(x);
        } 
      };
  }
  
  default UserDetailsService toSpring() {
    return username -> {
        try {
          return loadUserByUsername(username).toSpring();
        } catch (AcegiSecurityException x) {
          throw x.toSpring();
        } catch (DataAccessException x) {
          throw x.toSpring();
        } 
      };
  }
  
  UserDetails loadUserByUsername(String paramString) throws UsernameNotFoundException, DataAccessException;
}
