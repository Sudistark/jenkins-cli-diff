package org.acegisecurity.userdetails;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.Serializable;
import org.acegisecurity.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Deprecated
public interface UserDetails extends Serializable {
  GrantedAuthority[] getAuthorities();
  
  String getPassword();
  
  String getUsername();
  
  boolean isAccountNonExpired();
  
  boolean isAccountNonLocked();
  
  boolean isCredentialsNonExpired();
  
  boolean isEnabled();
  
  @NonNull
  default UserDetails toSpring() { return new UserDetailsSpringImpl(this); }
  
  @NonNull
  static UserDetails fromSpring(@NonNull UserDetails ud) {
    if (ud instanceof UserDetailsSpringImpl)
      return ((UserDetailsSpringImpl)ud).delegate; 
    return new Object(ud);
  }
  
  @Nullable
  static Object toSpringPrincipal(@CheckForNull Object acegiPrincipal) {
    if (acegiPrincipal instanceof UserDetails)
      return ((UserDetails)acegiPrincipal).toSpring(); 
    return acegiPrincipal;
  }
  
  @Nullable
  static Object fromSpringPrincipal(@CheckForNull Object springPrincipal) {
    if (springPrincipal instanceof UserDetails)
      return fromSpring((UserDetails)springPrincipal); 
    return springPrincipal;
  }
}
