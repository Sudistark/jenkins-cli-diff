package org.acegisecurity;

import java.util.Collection;
import org.acegisecurity.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Deprecated
final class AuthenticationSpringImpl implements Authentication {
  final Authentication delegate;
  
  AuthenticationSpringImpl(Authentication delegate) { this.delegate = delegate; }
  
  public Collection<? extends GrantedAuthority> getAuthorities() { return GrantedAuthority.toSpring(this.delegate.getAuthorities()); }
  
  public Object getCredentials() { return this.delegate.getCredentials(); }
  
  public Object getDetails() { return this.delegate.getDetails(); }
  
  public Object getPrincipal() { return UserDetails.toSpringPrincipal(this.delegate.getPrincipal()); }
  
  public boolean isAuthenticated() { return this.delegate.isAuthenticated(); }
  
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { this.delegate.setAuthenticated(isAuthenticated); }
  
  public String getName() { return this.delegate.getName(); }
  
  public boolean equals(Object o) { return (o instanceof Authentication && ((Authentication)o).getName().equals(getName())); }
  
  public int hashCode() { return getName().hashCode(); }
  
  public String toString() { return super.toString() + ": " + super.toString(); }
}
