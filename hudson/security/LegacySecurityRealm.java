package hudson.security;

import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public final class LegacySecurityRealm extends SecurityRealm implements AuthenticationManager {
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Descriptor<SecurityRealm> DESCRIPTOR;
  
  public SecurityRealm.SecurityComponents createSecurityComponents() { return new SecurityRealm.SecurityComponents(this); }
  
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (authentication instanceof ContainerAuthentication)
      return authentication; 
    return null;
  }
  
  public String getAuthenticationGatewayUrl() { return "j_security_check"; }
  
  public String getLoginUrl() { return "loginEntry"; }
  
  public Filter createFilter(FilterConfig filterConfig) {
    List<Filter> filters = new ArrayList<Filter>();
    filters.add(new BasicAuthenticationFilter());
    filters.addAll(commonFilters());
    return new ChainedServletFilter(filters);
  }
}
