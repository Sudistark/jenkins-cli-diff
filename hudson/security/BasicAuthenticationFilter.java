package hudson.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.User;
import hudson.util.Scrambler;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import jenkins.security.BasicApiTokenHelper;
import jenkins.security.SecurityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class BasicAuthenticationFilter implements Filter {
  private ServletContext servletContext;
  
  public void init(FilterConfig filterConfig) throws ServletException { this.servletContext = filterConfig.getServletContext(); }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest)request;
    HttpServletResponse rsp = (HttpServletResponse)response;
    String authorization = req.getHeader("Authorization");
    String path = req.getServletPath();
    if (authorization == null || req.getUserPrincipal() != null || path.startsWith("/secured/") || 
      !Jenkins.get().isUseSecurity()) {
      if (req.getUserPrincipal() != null)
        SecurityContextHolder.getContext().setAuthentication(new ContainerAuthentication(req)); 
      try {
        chain.doFilter(request, response);
      } finally {
        SecurityContextHolder.clearContext();
      } 
      return;
    } 
    String username = null;
    String password = null;
    String uidpassword = Scrambler.descramble(authorization.substring(6));
    int idx = uidpassword.indexOf(':');
    if (idx >= 0) {
      username = uidpassword.substring(0, idx);
      password = uidpassword.substring(idx + 1);
    } 
    if (username == null) {
      rsp.setStatus(401);
      rsp.setHeader("WWW-Authenticate", "Basic realm=\"Jenkins user\"");
      return;
    } 
    User u = BasicApiTokenHelper.isConnectingUsingApiToken(username, password);
    if (u != null) {
      UserDetails userDetails = u.getUserDetailsForImpersonation2();
      Authentication auth = u.impersonate(userDetails);
      SecurityListener.fireAuthenticated2(userDetails);
      SecurityContextHolder.getContext().setAuthentication(auth);
      try {
        chain.doFilter(request, response);
      } finally {
        SecurityContextHolder.clearContext();
      } 
      return;
    } 
    path = req.getContextPath() + "/secured" + req.getContextPath();
    String q = req.getQueryString();
    if (q != null)
      path = path + "?" + path; 
    prepareRedirect(rsp, path);
    RequestDispatcher d = this.servletContext.getRequestDispatcher("/j_security_check?j_username=" + 
        URLEncoder.encode(username, StandardCharsets.UTF_8) + "&j_password=" + URLEncoder.encode(password, StandardCharsets.UTF_8));
    d.include(req, rsp);
  }
  
  @SuppressFBWarnings(value = {"UNVALIDATED_REDIRECT"}, justification = "Redirect is validated as processed.")
  private void prepareRedirect(HttpServletResponse rsp, String path) {
    rsp.setStatus(302);
    rsp.setHeader("Location", path);
  }
  
  public void destroy() {}
}
