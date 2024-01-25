package org.acegisecurity.providers.anonymous;

import java.io.Serializable;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

@Deprecated
public class AnonymousAuthenticationToken implements Authentication, Serializable {
  private final AnonymousAuthenticationToken delegate;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public AnonymousAuthenticationToken(AnonymousAuthenticationToken delegate) { this.delegate = delegate; }
  
  public AnonymousAuthenticationToken(String key, Object principal, GrantedAuthority[] authorities) { this(new AnonymousAuthenticationToken(key, UserDetails.toSpringPrincipal(principal), GrantedAuthority.toSpring(authorities))); }
  
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
