package org.acegisecurity.userdetails;

import java.util.Collection;
import org.acegisecurity.GrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Deprecated
final class UserDetailsSpringImpl implements UserDetails {
  final UserDetails delegate;
  
  UserDetailsSpringImpl(UserDetails delegate) { this.delegate = delegate; }
  
  public Collection<? extends GrantedAuthority> getAuthorities() { return GrantedAuthority.toSpring(this.delegate.getAuthorities()); }
  
  public String getPassword() { return this.delegate.getPassword(); }
  
  public String getUsername() { return this.delegate.getUsername(); }
  
  public boolean isAccountNonExpired() { return this.delegate.isAccountNonExpired(); }
  
  public boolean isAccountNonLocked() { return this.delegate.isAccountNonLocked(); }
  
  public boolean isCredentialsNonExpired() { return this.delegate.isCredentialsNonExpired(); }
  
  public boolean isEnabled() { return this.delegate.isEnabled(); }
}
