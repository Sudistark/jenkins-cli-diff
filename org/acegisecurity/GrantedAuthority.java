package org.acegisecurity;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.SecurityRealm;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Deprecated
public interface GrantedAuthority {
  @NonNull
  static GrantedAuthority fromSpring(@NonNull GrantedAuthority ga) {
    if (ga == SecurityRealm.AUTHENTICATED_AUTHORITY2)
      return SecurityRealm.AUTHENTICATED_AUTHORITY; 
    return new GrantedAuthorityImpl(ga.getAuthority());
  }
  
  @NonNull
  default GrantedAuthority toSpring() {
    if (this == SecurityRealm.AUTHENTICATED_AUTHORITY)
      return SecurityRealm.AUTHENTICATED_AUTHORITY2; 
    return new SimpleGrantedAuthority(getAuthority());
  }
  
  @NonNull
  static GrantedAuthority[] fromSpring(@NonNull Collection<? extends GrantedAuthority> gas) { return (GrantedAuthority[])gas.stream().map(GrantedAuthority::fromSpring).toArray(x$0 -> new GrantedAuthority[x$0]); }
  
  @NonNull
  static Collection<? extends GrantedAuthority> toSpring(@NonNull GrantedAuthority[] gas) { return (gas != null) ? (Collection)Stream.of(gas).map(GrantedAuthority::toSpring).collect(Collectors.toList()) : Collections.emptySet(); }
  
  String getAuthority();
  
  String toString();
  
  boolean equals(Object paramObject);
  
  int hashCode();
}
