package jenkins.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Descriptor;
import hudson.model.ReconfigurableDescribable;
import hudson.model.UserProperty;
import hudson.security.SecurityRealm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.acegisecurity.GrantedAuthority;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class LastGrantedAuthoritiesProperty extends UserProperty {
  private long timestamp;
  
  public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException {
    req.bindJSON(this, form);
    return this;
  }
  
  public Collection<? extends GrantedAuthority> getAuthorities2() {
    String[] roles = this.roles;
    if (roles == null)
      return Set.of(SecurityRealm.AUTHENTICATED_AUTHORITY2); 
    String authenticatedRole = SecurityRealm.AUTHENTICATED_AUTHORITY2.getAuthority();
    List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(roles.length + 1);
    grantedAuthorities.add(new SimpleGrantedAuthority(authenticatedRole));
    for (String role : roles) {
      if (!authenticatedRole.equals(role))
        grantedAuthorities.add(new SimpleGrantedAuthority(role)); 
    } 
    return grantedAuthorities;
  }
  
  @Deprecated
  public GrantedAuthority[] getAuthorities() { return GrantedAuthority.fromSpring(getAuthorities2()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void update(@NonNull Authentication auth) throws IOException {
    List<String> roles = new ArrayList<String>();
    for (GrantedAuthority ga : auth.getAuthorities())
      roles.add(ga.getAuthority()); 
    String[] a = (String[])roles.toArray(new String[0]);
    if (!Arrays.equals(this.roles, a)) {
      this.roles = a;
      this.timestamp = System.currentTimeMillis();
      this.user.save();
    } 
  }
  
  public void invalidate() {
    if (this.roles != null) {
      this.roles = null;
      this.timestamp = System.currentTimeMillis();
      this.user.save();
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(LastGrantedAuthoritiesProperty.class.getName());
}
