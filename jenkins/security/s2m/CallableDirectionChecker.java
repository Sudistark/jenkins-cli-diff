package jenkins.security.s2m;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.security.Roles;
import jenkins.util.SystemProperties;
import org.jenkinsci.remoting.Role;
import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CallableDirectionChecker extends RoleChecker {
  private static final String BYPASS_PROP = CallableDirectionChecker.class.getName() + ".allow";
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean BYPASS = SystemProperties.getBoolean(BYPASS_PROP);
  
  public void check(RoleSensitive subject, @NonNull Collection<Role> expected) throws SecurityException {
    String name = subject.getClass().getName();
    if (expected.contains(Roles.MASTER)) {
      LOGGER.log(Level.FINE, "Executing {0} is allowed since it is targeted for the controller role", name);
      return;
    } 
    if (BYPASS) {
      LOGGER.log(Level.FINE, "Allowing {0} to be sent from agent to controller because bypass is set", name);
      return;
    } 
    throw new SecurityException("Sending " + name + " from agent to controller is prohibited.\nSee https://www.jenkins.io/redirect/security-144 for more details");
  }
  
  private static final Logger LOGGER = Logger.getLogger(CallableDirectionChecker.class.getName());
}
