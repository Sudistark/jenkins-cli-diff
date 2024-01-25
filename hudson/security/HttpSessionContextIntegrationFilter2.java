package hudson.security;

import hudson.model.User;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import jenkins.security.seed.UserSeedProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.SecurityContextRepository;

public class HttpSessionContextIntegrationFilter2 extends SecurityContextPersistenceFilter {
  public HttpSessionContextIntegrationFilter2(SecurityContextRepository securityContextRepository) { super(securityContextRepository); }
  
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpSession session = ((HttpServletRequest)req).getSession(false);
    if (session != null) {
      SecurityContext o = (SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
      if (o != null) {
        Authentication a = o.getAuthentication();
        if (a != null && 
          hasInvalidSessionSeed(a, session))
          session.setAttribute("SPRING_SECURITY_CONTEXT", null); 
      } 
    } 
    super.doFilter(req, res, chain);
  }
  
  private boolean hasInvalidSessionSeed(Authentication authentication, HttpSession session) {
    String actualUserSessionSeed;
    User userFromSession;
    if (UserSeedProperty.DISABLE_USER_SEED || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
      return false; 
    try {
      userFromSession = User.getById(authentication.getName(), false);
    } catch (IllegalStateException ise) {
      this.logger.warn("Encountered IllegalStateException trying to get a user. System init may not have completed yet. Invalidating user session.");
      return false;
    } 
    if (userFromSession == null)
      return false; 
    Object userSessionSeedObject = session.getAttribute("_JENKINS_SESSION_SEED");
    if (userSessionSeedObject instanceof String) {
      actualUserSessionSeed = (String)userSessionSeedObject;
    } else {
      return true;
    } 
    UserSeedProperty userSeedProperty = (UserSeedProperty)userFromSession.getProperty(UserSeedProperty.class);
    if (userSeedProperty == null)
      return true; 
    boolean validSeed = actualUserSessionSeed.equals(userSeedProperty.getSeed());
    return !validSeed;
  }
}
