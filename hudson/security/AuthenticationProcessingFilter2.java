package hudson.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jenkins.security.SecurityListener;
import jenkins.security.seed.UserSeedProperty;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class AuthenticationProcessingFilter2 extends UsernamePasswordAuthenticationFilter {
  @SuppressFBWarnings(value = {"HARD_CODE_PASSWORD"}, justification = "This is a password parameter, not a password")
  public AuthenticationProcessingFilter2(String authenticationGatewayUrl) {
    setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/" + authenticationGatewayUrl, "POST"));
    setUsernameParameter("j_username");
    setPasswordParameter("j_password");
  }
  
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT"}, justification = "request.getSession(true) does in fact have a side effect")
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
    if (SystemProperties.getInteger(SecurityRealm.class.getName() + ".sessionFixationProtectionMode", Integer.valueOf(1)).intValue() == 2) {
      request.getSession().invalidate();
      request.getSession(true);
    } 
    super.successfulAuthentication(request, response, chain, authResult);
    HttpSession newSession = request.getSession();
    if (!UserSeedProperty.DISABLE_USER_SEED) {
      User user = User.getById(authResult.getName(), true);
      UserSeedProperty userSeed = (UserSeedProperty)user.getProperty(UserSeedProperty.class);
      String sessionSeed = userSeed.getSeed();
      newSession.setAttribute("_JENKINS_SESSION_SEED", sessionSeed);
    } 
    SecurityListener.fireLoggedIn(authResult.getName());
  }
  
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
    super.unsuccessfulAuthentication(request, response, failed);
    LOGGER.log(Level.FINE, "Login attempt failed", failed);
  }
  
  private static final Logger LOGGER = Logger.getLogger(AuthenticationProcessingFilter2.class.getName());
}
