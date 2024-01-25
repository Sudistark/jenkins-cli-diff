package jenkins.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.security.SecurityRealm;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public abstract class SecurityListener implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(SecurityListener.class.getName());
  
  protected void authenticated2(@NonNull UserDetails details) { authenticated(UserDetails.fromSpring(details)); }
  
  @Deprecated
  protected void authenticated(@NonNull UserDetails details) {}
  
  protected void failedToAuthenticate(@NonNull String username) {}
  
  protected void loggedIn(@NonNull String username) {}
  
  protected void userCreated(@NonNull String username) {}
  
  protected void failedToLogIn(@NonNull String username) {}
  
  protected void loggedOut(@NonNull String username) {}
  
  public static void fireAuthenticated2(@NonNull UserDetails details) {
    if (LOGGER.isLoggable(Level.FINE)) {
      List<String> groups = new ArrayList<String>();
      for (GrantedAuthority auth : details.getAuthorities()) {
        if (!auth.equals(SecurityRealm.AUTHENTICATED_AUTHORITY2))
          groups.add(auth.getAuthority()); 
      } 
      LOGGER.log(Level.FINE, "authenticated: {0} {1}", new Object[] { details.getUsername(), groups });
    } 
    for (SecurityListener l : all())
      l.authenticated2(details); 
  }
  
  @Deprecated
  public static void fireAuthenticated(@NonNull UserDetails details) { fireAuthenticated2(details.toSpring()); }
  
  public static void fireUserCreated(@NonNull String username) {
    LOGGER.log(Level.FINE, "new user created: {0}", username);
    for (SecurityListener l : all())
      l.userCreated(username); 
  }
  
  public static void fireFailedToAuthenticate(@NonNull String username) {
    LOGGER.log(Level.FINE, "failed to authenticate: {0}", username);
    for (SecurityListener l : all())
      l.failedToAuthenticate(username); 
  }
  
  public static void fireLoggedIn(@NonNull String username) {
    LOGGER.log(Level.FINE, "logged in: {0}", username);
    for (SecurityListener l : all())
      l.loggedIn(username); 
  }
  
  public static void fireFailedToLogIn(@NonNull String username) {
    LOGGER.log(Level.FINE, "failed to log in: {0}", username);
    for (SecurityListener l : all())
      l.failedToLogIn(username); 
  }
  
  public static void fireLoggedOut(@NonNull String username) {
    LOGGER.log(Level.FINE, "logged out: {0}", username);
    for (SecurityListener l : all())
      l.loggedOut(username); 
  }
  
  private static List<SecurityListener> all() { return ExtensionList.lookup(SecurityListener.class); }
}
