package jenkins.security;

import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

public abstract class SlaveToMasterCallable<V, T extends Throwable> extends Object implements Callable<V, T> {
  private static final long serialVersionUID = 1L;
  
  public void checkRoles(RoleChecker checker) throws SecurityException { checker.check(this, Roles.MASTER); }
}
