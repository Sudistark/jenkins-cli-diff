package hudson.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class ContainerAuthentication implements Authentication {
  private final Principal principal;
  
  private Collection<? extends GrantedAuthority> authorities;
  
  public ContainerAuthentication(HttpServletRequest request) {
    this.principal = request.getUserPrincipal();
    if (this.principal == null)
      throw new IllegalStateException(); 
    List<GrantedAuthority> l = new ArrayList<GrantedAuthority>();
    for (String g : Jenkins.get().getAuthorizationStrategy().getGroups()) {
      if (request.isUserInRole(g))
        l.add(new SimpleGrantedAuthority(g)); 
    } 
    l.add(SecurityRealm.AUTHENTICATED_AUTHORITY2);
    this.authorities = l;
  }
  
  public Collection<? extends GrantedAuthority> getAuthorities() { return this.authorities; }
  
  public Object getCredentials() { return null; }
  
  public Object getDetails() { return null; }
  
  public String getPrincipal() { return this.principal.getName(); }
  
  public boolean isAuthenticated() { return true; }
  
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
  
  public String getName() { return getPrincipal(); }
}
