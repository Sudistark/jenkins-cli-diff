package hudson.security;

import java.util.Collection;
import java.util.Set;

public final class LegacyAuthorizationStrategy extends AuthorizationStrategy {
  private static final ACL LEGACY_ACL = new Object(null);
  
  public ACL getRootACL() { return LEGACY_ACL; }
  
  public Collection<String> getGroups() { return Set.of("admin"); }
}
