package jenkins.security.seed;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import javax.servlet.http.HttpSession;
import jenkins.security.SecurityListener;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.security.core.userdetails.UserDetails;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension(ordinal = 2.147483647E9D)
public class UserSeedSecurityListener extends SecurityListener {
  protected void loggedIn(@NonNull String username) { putUserSeedInSession(username, true); }
  
  protected void authenticated2(@NonNull UserDetails details) { putUserSeedInSession(details.getUsername(), false); }
  
  private static void putUserSeedInSession(String username, boolean overwriteSessionSeed) {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req == null)
      return; 
    HttpSession session = req.getSession(false);
    if (session == null)
      return; 
    if (!UserSeedProperty.DISABLE_USER_SEED) {
      if (!overwriteSessionSeed && session.getAttribute("_JENKINS_SESSION_SEED") != null)
        return; 
      User user = User.getById(username, true);
      UserSeedProperty userSeed = (UserSeedProperty)user.getProperty(UserSeedProperty.class);
      if (userSeed == null)
        return; 
      String sessionSeed = userSeed.getSeed();
      session.setAttribute("_JENKINS_SESSION_SEED", sessionSeed);
    } 
  }
}
