package hudson.security;

import hudson.model.Descriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;

public class FullControlOnceLoggedInAuthorizationStrategy extends AuthorizationStrategy {
  private boolean denyAnonymousReadAccess = false;
  
  public ACL getRootACL() { return this.denyAnonymousReadAccess ? AUTHENTICATED_READ : ANONYMOUS_READ; }
  
  public List<String> getGroups() { return Collections.emptyList(); }
  
  public boolean isAllowAnonymousRead() { return !this.denyAnonymousReadAccess; }
  
  @DataBoundSetter
  public void setAllowAnonymousRead(boolean allowAnonymousRead) { this.denyAnonymousReadAccess = !allowAnonymousRead; }
  
  private static final SparseACL AUTHENTICATED_READ = new SparseACL(null);
  
  private static final SparseACL ANONYMOUS_READ = new SparseACL(null);
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Descriptor<AuthorizationStrategy> DESCRIPTOR;
  
  static  {
    ANONYMOUS_READ.add(ACL.EVERYONE, Jenkins.ADMINISTER, true);
    ANONYMOUS_READ.add(ACL.ANONYMOUS, Jenkins.ADMINISTER, false);
    ANONYMOUS_READ.add(ACL.ANONYMOUS, Permission.READ, true);
    AUTHENTICATED_READ.add(ACL.EVERYONE, Jenkins.ADMINISTER, true);
    AUTHENTICATED_READ.add(ACL.ANONYMOUS, Jenkins.ADMINISTER, false);
  }
}
