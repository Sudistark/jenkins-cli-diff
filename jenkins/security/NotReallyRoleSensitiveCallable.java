package jenkins.security;

import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

public abstract class NotReallyRoleSensitiveCallable<V, T extends Throwable> extends Object implements Callable<V, T> {
  public void checkRoles(RoleChecker checker) throws SecurityException { throw new UnsupportedOperationException(); }
}
