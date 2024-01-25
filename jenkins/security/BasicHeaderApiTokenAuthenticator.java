package jenkins.security;

import hudson.Extension;
import hudson.model.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class BasicHeaderApiTokenAuthenticator extends BasicHeaderAuthenticator {
  public Authentication authenticate2(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws ServletException {
    User u = BasicApiTokenHelper.isConnectingUsingApiToken(username, password);
    if (u != null) {
      Authentication auth;
      try {
        UserDetails userDetails = u.getUserDetailsForImpersonation2();
        auth = u.impersonate(userDetails);
        SecurityListener.fireAuthenticated2(userDetails);
      } catch (UsernameNotFoundException x) {
        LOGGER.log(Level.WARNING, "API token matched for user " + username + " but the impersonation failed", x);
        throw new ServletException(x);
      } 
      req.setAttribute(BasicHeaderApiTokenAuthenticator.class.getName(), Boolean.valueOf(true));
      return auth;
    } 
    return null;
  }
  
  private static final Logger LOGGER = Logger.getLogger(BasicHeaderApiTokenAuthenticator.class.getName());
}
