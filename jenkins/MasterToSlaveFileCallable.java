package jenkins;

import hudson.FilePath;
import jenkins.security.Roles;
import org.jenkinsci.remoting.RoleChecker;

public abstract class MasterToSlaveFileCallable<T> extends Object implements FilePath.FileCallable<T> {
  private static final long serialVersionUID = 1L;
  
  public void checkRoles(RoleChecker checker) throws SecurityException { checker.check(this, Roles.SLAVE); }
}
