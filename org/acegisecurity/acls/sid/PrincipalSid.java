package org.acegisecurity.acls.sid;

import java.util.Objects;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class PrincipalSid implements Sid {
  private final String principal;
  
  public PrincipalSid(String principal) { this.principal = principal; }
  
  public PrincipalSid(Authentication a) {
    Object p = a.getPrincipal();
    this.principal = (p instanceof UserDetails) ? ((UserDetails)p).getUsername() : p.toString();
  }
  
  @Deprecated
  public PrincipalSid(Authentication a) { this(a.toSpring()); }
  
  public String getPrincipal() { return this.principal; }
  
  public boolean equals(Object o) { return (o instanceof PrincipalSid && Objects.equals(this.principal, ((PrincipalSid)o).principal)); }
  
  public int hashCode() { return this.principal.hashCode(); }
  
  public String toString() { return this.principal; }
}
