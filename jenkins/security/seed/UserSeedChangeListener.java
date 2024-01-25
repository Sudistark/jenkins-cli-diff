package jenkins.security.seed;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.User;
import java.util.List;
import java.util.logging.Logger;
import jenkins.util.Listeners;

public abstract class UserSeedChangeListener implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(jenkins.security.SecurityListener.class.getName());
  
  public static void fireUserSeedRenewed(@NonNull User user) { Listeners.notify(UserSeedChangeListener.class, true, l -> l.onUserSeedRenewed(user)); }
  
  private static List<UserSeedChangeListener> all() { return ExtensionList.lookup(UserSeedChangeListener.class); }
  
  public abstract void onUserSeedRenewed(@NonNull User paramUser);
}
