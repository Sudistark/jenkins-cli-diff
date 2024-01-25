package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public interface AccessControlled {
  @NonNull
  ACL getACL();
  
  default void checkPermission(@NonNull Permission permission) throws AccessDeniedException {
    if (Jenkins.getAuthentication2().equals(ACL.SYSTEM2))
      return; 
    getACL().checkPermission(permission);
  }
  
  void checkAnyPermission(@NonNull Permission... permission) throws AccessDeniedException { getACL().checkAnyPermission(permission); }
  
  default boolean hasPermission(@NonNull Permission permission) {
    if (Jenkins.getAuthentication2().equals(ACL.SYSTEM2))
      return true; 
    return getACL().hasPermission(permission);
  }
  
  boolean hasAnyPermission(@NonNull Permission... permission) { return getACL().hasAnyPermission(permission); }
  
  default boolean hasPermission2(@NonNull Authentication a, @NonNull Permission permission) {
    if (a.equals(ACL.SYSTEM2))
      return true; 
    return getACL().hasPermission2(a, permission);
  }
  
  @Deprecated
  default boolean hasPermission(@NonNull Authentication a, @NonNull Permission permission) { return hasPermission2(a.toSpring(), permission); }
}
