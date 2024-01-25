package org.acegisecurity;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.ACL;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Deprecated
public interface Authentication extends Principal, Serializable {
  GrantedAuthority[] getAuthorities();
  
  Object getCredentials();
  
  Object getDetails();
  
  Object getPrincipal();
  
  boolean isAuthenticated();
  
  void setAuthenticated(boolean paramBoolean) throws IllegalArgumentException;
  
  @NonNull
  static Authentication fromSpring(@NonNull Authentication a) {
    Objects.requireNonNull(a);
    if (a == ACL.SYSTEM2)
      return ACL.SYSTEM; 
    if (a instanceof AnonymousAuthenticationToken)
      return new AnonymousAuthenticationToken((AnonymousAuthenticationToken)a); 
    if (a instanceof UsernamePasswordAuthenticationToken)
      return new UsernamePasswordAuthenticationToken((UsernamePasswordAuthenticationToken)a); 
    if (a instanceof AuthenticationSpringImpl)
      return ((AuthenticationSpringImpl)a).delegate; 
    return new Object(a);
  }
  
  @NonNull
  default Authentication toSpring() { return new AuthenticationSpringImpl(this); }
}
