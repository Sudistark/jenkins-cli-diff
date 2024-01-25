package hudson.security;

import hudson.ExtensionPoint;
import hudson.model.User;

public abstract class PermissionAdder implements ExtensionPoint {
  public abstract boolean add(AuthorizationStrategy paramAuthorizationStrategy, User paramUser, Permission paramPermission);
}
