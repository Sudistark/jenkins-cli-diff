package jenkins;

import hudson.FilePath;
import hudson.Main;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.security.Roles;
import jenkins.util.JenkinsJVM;
import org.jenkinsci.remoting.RoleChecker;

@Deprecated
public abstract class SlaveToMasterFileCallable<T> extends Object implements FilePath.FileCallable<T> {
  public static final Logger LOGGER = Logger.getLogger(SlaveToMasterFileCallable.class.getName());
  
  private static final long serialVersionUID = 1L;
  
  public void checkRoles(RoleChecker checker) throws SecurityException {
    warnOnController();
    checker.check(this, Roles.MASTER);
  }
  
  protected Object readResolve() {
    warnOnController();
    return this;
  }
  
  private void warnOnController() {
    if (JenkinsJVM.isJenkinsJVM() && (Main.isUnitTest || Main.isDevelopmentMode))
      LOGGER.log(Level.WARNING, "SlaveToMasterFileCallable is deprecated. '" + this + "' should be replaced. See https://www.jenkins.io/doc/developer/security/remoting-callables/"); 
  }
}
