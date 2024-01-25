package hudson.security;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;

public class HudsonFilter implements Filter {
  private FilterConfig filterConfig;
  
  @Deprecated
  public static final AuthenticationManagerProxy AUTHENTICATION_MANAGER = new AuthenticationManagerProxy();
  
  @Deprecated
  public static final UserDetailsServiceProxy USER_DETAILS_SERVICE_PROXY = new UserDetailsServiceProxy();
  
  @Deprecated
  public static final RememberMeServicesProxy REMEMBER_ME_SERVICES_PROXY = new RememberMeServicesProxy();
  
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
    filterConfig.getServletContext().setAttribute(HudsonFilter.class.getName(), this);
    try {
      Jenkins hudson = Jenkins.getInstanceOrNull();
      if (hudson != null) {
        LOGGER.fine("Security wasn't initialized; Initializing it...");
        SecurityRealm securityRealm = hudson.getSecurityRealm();
        reset(securityRealm);
        LOGGER.fine("securityRealm is " + securityRealm);
        LOGGER.fine("Security initialized");
      } 
    } catch (ExceptionInInitializerError e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize Jenkins", e);
    } 
  }
  
  public static HudsonFilter get(ServletContext context) { return (HudsonFilter)context.getAttribute(HudsonFilter.class.getName()); }
  
  public void reset(SecurityRealm securityRealm) throws ServletException {
    if (securityRealm != null) {
      SecurityRealm.SecurityComponents sc = securityRealm.getSecurityComponents();
      AUTHENTICATION_MANAGER.setDelegate(sc.manager2);
      USER_DETAILS_SERVICE_PROXY.setDelegate(sc.userDetails2);
      REMEMBER_ME_SERVICES_PROXY.setDelegate(sc.rememberMe2);
      Filter oldf = this.filter;
      Filter newf = securityRealm.createFilter(this.filterConfig);
      newf.init(this.filterConfig);
      this.filter = newf;
      if (oldf != null)
        oldf.destroy(); 
    } else {
      AUTHENTICATION_MANAGER.setDelegate(null);
      USER_DETAILS_SERVICE_PROXY.setDelegate(null);
      REMEMBER_ME_SERVICES_PROXY.setDelegate(null);
      this.filter = null;
    } 
  }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    LOGGER.entering(HudsonFilter.class.getName(), "doFilter");
    ((HttpServletResponse)response).setHeader("X-Content-Type-Options", "nosniff");
    Filter f = this.filter;
    if (f == null) {
      chain.doFilter(request, response);
    } else {
      f.doFilter(request, response, chain);
    } 
  }
  
  public void destroy() {
    if (this.filter != null)
      this.filter.destroy(); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(HudsonFilter.class.getName());
}
