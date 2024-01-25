package org.acegisecurity.userdetails;

import org.acegisecurity.GrantedAuthority;

@Deprecated
public class User implements UserDetails {
  private static final long serialVersionUID = 1L;
  
  private final String username;
  
  private final String password;
  
  private final boolean enabled;
  
  private final boolean accountNonExpired;
  
  private final boolean credentialsNonExpired;
  
  private final boolean accountNonLocked;
  
  private GrantedAuthority[] authorities;
  
  public User(String username, String password, boolean enabled, GrantedAuthority[] authorities) { this(username, password, enabled, true, true, true, authorities); }
  
  public User(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, GrantedAuthority[] authorities) { this(username, password, enabled, accountNonExpired, credentialsNonExpired, true, authorities); }
  
  public User(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, GrantedAuthority[] authorities) {
    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.accountNonExpired = accountNonExpired;
    this.credentialsNonExpired = credentialsNonExpired;
    this.accountNonLocked = accountNonLocked;
    setAuthorities(authorities);
  }
  
  public GrantedAuthority[] getAuthorities() { return this.authorities; }
  
  protected void setAuthorities(GrantedAuthority[] authorities) { this.authorities = authorities; }
  
  public String getPassword() { return this.password; }
  
  public String getUsername() { return this.username; }
  
  public boolean isAccountNonExpired() { return this.accountNonExpired; }
  
  public boolean isAccountNonLocked() { return this.accountNonLocked; }
  
  public boolean isCredentialsNonExpired() { return this.credentialsNonExpired; }
  
  public boolean isEnabled() { return this.enabled; }
  
  public boolean equals(Object o) { return (o instanceof UserDetails && ((UserDetails)o).getUsername().equals(getUsername())); }
  
  public int hashCode() { return getUsername().hashCode(); }
  
  public String toString() { return getUsername(); }
}
