package org.acegisecurity;

import java.io.Serializable;

@Deprecated
public class GrantedAuthorityImpl implements GrantedAuthority, Serializable {
  private static final long serialVersionUID = 1L;
  
  private final String role;
  
  public GrantedAuthorityImpl(String role) { this.role = role; }
  
  public String getAuthority() { return this.role; }
  
  public String toString() { return this.role; }
  
  public boolean equals(Object o) { return (o instanceof GrantedAuthorityImpl && this.role.equals(((GrantedAuthorityImpl)o).role)); }
  
  public int hashCode() { return this.role.hashCode(); }
}
