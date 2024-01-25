package org.acegisecurity.acls.sid;

import java.util.Objects;
import org.acegisecurity.GrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

public class GrantedAuthoritySid implements Sid {
  private final String grantedAuthority;
  
  public GrantedAuthoritySid(String grantedAuthority) { this.grantedAuthority = grantedAuthority; }
  
  public GrantedAuthoritySid(GrantedAuthority ga) { this.grantedAuthority = ga.getAuthority(); }
  
  @Deprecated
  public GrantedAuthoritySid(GrantedAuthority ga) { this(ga.toSpring()); }
  
  public String getGrantedAuthority() { return this.grantedAuthority; }
  
  public boolean equals(Object o) { return (o instanceof GrantedAuthoritySid && Objects.equals(this.grantedAuthority, ((GrantedAuthoritySid)o).grantedAuthority)); }
  
  public int hashCode() { return this.grantedAuthority.hashCode(); }
  
  public String toString() { return this.grantedAuthority; }
}
