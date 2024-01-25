package org.acegisecurity.providers;

import java.util.Arrays;
import java.util.Objects;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

@Deprecated
public abstract class AbstractAuthenticationToken implements Authentication {
  private final GrantedAuthority[] authorities;
  
  private Object details;
  
  private boolean authenticated;
  
  protected AbstractAuthenticationToken() { this.authorities = new GrantedAuthority[0]; }
  
  protected AbstractAuthenticationToken(GrantedAuthority[] authorities) { this.authorities = authorities; }
  
  public String getName() {
    Object principal = getPrincipal();
    return (principal instanceof UserDetails) ? ((UserDetails)principal).getUsername() : String.valueOf(principal);
  }
  
  public GrantedAuthority[] getAuthorities() { return this.authorities; }
  
  public Object getDetails() { return this.details; }
  
  public void setDetails(Object details) { this.details = details; }
  
  public boolean isAuthenticated() { return this.authenticated; }
  
  public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
  
  public String toString() { return super.toString() + super.toString(); }
  
  public boolean equals(Object o) {
    return (o instanceof AbstractAuthenticationToken && 
      Objects.equals(getPrincipal(), ((AbstractAuthenticationToken)o).getPrincipal()) && 
      Objects.equals(getDetails(), ((AbstractAuthenticationToken)o).getDetails()) && 
      Objects.equals(getCredentials(), ((AbstractAuthenticationToken)o).getCredentials()) && 
      isAuthenticated() == ((AbstractAuthenticationToken)o).isAuthenticated() && 
      Arrays.equals(getAuthorities(), ((AbstractAuthenticationToken)o).getAuthorities()));
  }
  
  public int hashCode() { return getName().hashCode(); }
}
