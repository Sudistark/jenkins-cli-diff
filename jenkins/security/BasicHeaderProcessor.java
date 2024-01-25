package jenkins.security;

import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.Scrambler;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class BasicHeaderProcessor implements Filter {
  private AuthenticationEntryPoint authenticationEntryPoint;
  
  private RememberMeServices rememberMeServices = new NullRememberMeServices();
  
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) { this.authenticationEntryPoint = authenticationEntryPoint; }
  
  public void setRememberMeServices(RememberMeServices rememberMeServices) { this.rememberMeServices = rememberMeServices; }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest)request;
    HttpServletResponse rsp = (HttpServletResponse)response;
    String authorization = req.getHeader("Authorization");
    if (StringUtils.startsWithIgnoreCase(authorization, "Basic ")) {
      String uidpassword = Scrambler.descramble(authorization.substring(6));
      int idx = uidpassword.indexOf(':');
      if (idx >= 0) {
        String username = uidpassword.substring(0, idx);
        String password = uidpassword.substring(idx + 1);
        if (!authenticationIsRequired(username)) {
          chain.doFilter(request, response);
          return;
        } 
        for (BasicHeaderAuthenticator a : all()) {
          LOGGER.log(Level.FINER, "Attempting to authenticate with {0}", a);
          Authentication auth = a.authenticate2(req, rsp, username, password);
          if (auth != null) {
            LOGGER.log(Level.FINE, "Request authenticated as {0} by {1}", new Object[] { auth, a });
            success(req, rsp, chain, auth);
            return;
          } 
        } 
        fail(req, rsp, new BadCredentialsException("Invalid password/token for user: " + username));
      } else {
        fail(req, rsp, new BadCredentialsException("Malformed HTTP basic Authorization header"));
      } 
    } else {
      chain.doFilter(request, response);
    } 
  }
  
  protected boolean authenticationIsRequired(String username) {
    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if (existingAuth == null || !existingAuth.isAuthenticated())
      return true; 
    if (existingAuth instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken && !existingAuth.getName().equals(username))
      return true; 
    return existingAuth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
  }
  
  protected void success(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain, Authentication auth) throws IOException, ServletException {
    this.rememberMeServices.loginSuccess(req, rsp, auth);
    ACLContext ctx = ACL.as2(auth);
    try {
      chain.doFilter(req, rsp);
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  protected void fail(HttpServletRequest req, HttpServletResponse rsp, BadCredentialsException failure) throws IOException, ServletException {
    LOGGER.log(Level.FINE, "Authentication of BASIC header failed");
    this.rememberMeServices.loginFail(req, rsp);
    this.authenticationEntryPoint.commence(req, rsp, failure);
  }
  
  protected List<? extends BasicHeaderAuthenticator> all() { return BasicHeaderAuthenticator.all(); }
  
  public void destroy() {}
  
  private static final Logger LOGGER = Logger.getLogger(BasicHeaderProcessor.class.getName());
}
