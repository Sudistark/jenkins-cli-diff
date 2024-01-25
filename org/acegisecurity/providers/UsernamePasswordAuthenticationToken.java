package org.acegisecurity.providers;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Deprecated
public class UsernamePasswordAuthenticationToken implements Authentication {
  private final UsernamePasswordAuthenticationToken delegate;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public UsernamePasswordAuthenticationToken(UsernamePasswordAuthenticationToken delegate) { this.delegate = delegate; }
  
  public UsernamePasswordAuthenticationToken(Object principal, Object credentials) { this(new UsernamePasswordAuthenticationToken(UserDetails.toSpringPrincipal(principal), credentials)); }
  
  public UsernamePasswordAuthenticationToken(Object principal, Object credentials, GrantedAuthority[] authorities) { this(new UsernamePasswordAuthenticationToken(UserDetails.toSpringPrincipal(principal), credentials, GrantedAuthority.toSpring(authorities))); }
  
  public GrantedAuthority[] getAuthorities() { return GrantedAuthority.fromSpring(this.delegate.getAuthorities()); }
  
  public Object getCredentials() { return this.delegate.getCredentials(); }
  
  public Object getDetails() { return this.delegate.getDetails(); }
  
  public void setDetails(Object details) { this.delegate.setDetails(details); }
  
  public Object getPrincipal() { return UserDetails.fromSpringPrincipal(this.delegate.getPrincipal()); }
  
  public boolean isAuthenticated() { return this.delegate.isAuthenticated(); }
  
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { this.delegate.setAuthenticated(isAuthenticated); }
  
  public String getName() { return this.delegate.getName(); }
  
  public boolean equals(Object o) { return (o instanceof Authentication && ((Authentication)o).getName().equals(getName())); }
  
  public int hashCode() { return getName().hashCode(); }
  
  public String toString() { return super.toString() + ": " + super.toString(); }
  
  public Authentication toSpring() { return this.delegate; }
}
