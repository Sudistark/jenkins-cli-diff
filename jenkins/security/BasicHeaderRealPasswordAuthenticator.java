package jenkins.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class BasicHeaderRealPasswordAuthenticator extends BasicHeaderAuthenticator {
  private AuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();
  
  public Authentication authenticate2(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws IOException, ServletException {
    if (DISABLE)
      return null; 
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
    authRequest.setDetails(this.authenticationDetailsSource.buildDetails(req));
    try {
      Authentication a = (Jenkins.get().getSecurityRealm().getSecurityComponents()).manager2.authenticate(authRequest);
      LOGGER.log(Level.FINER, "Authentication success: {0}", a);
      return a;
    } catch (AuthenticationException failed) {
      LOGGER.log(Level.FINER, "Authentication request for user: {0} failed: {1}", new Object[] { username, failed });
      return null;
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(BasicHeaderRealPasswordAuthenticator.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLE = SystemProperties.getBoolean("jenkins.security.ignoreBasicAuth");
}
