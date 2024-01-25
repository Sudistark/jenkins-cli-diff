package hudson.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.User;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.model.ViewGroup;
import hudson.remoting.Callable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import jenkins.security.NonSerializableSecurityContext;
import org.acegisecurity.Authentication;
import org.acegisecurity.acls.sid.PrincipalSid;
import org.acegisecurity.acls.sid.Sid;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ACL {
  public final void checkPermission(@NonNull Permission p) {
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(SYSTEM2))
      return; 
    if (!hasPermission2(a, p)) {
      while (!p.enabled && p.impliedBy != null)
        p = p.impliedBy; 
      throw new AccessDeniedException3(a, p);
    } 
  }
  
  public final void checkAnyPermission(@NonNull Permission... permissions) {
    if (permissions.length == 0)
      throw new IllegalArgumentException("At least one permission must be provided"); 
    boolean failed = !hasAnyPermission(permissions);
    Authentication authentication = Jenkins.getAuthentication2();
    if (failed) {
      String errorMessage;
      Set<Permission> enabledPermissions = new LinkedHashSet<Permission>();
      for (Permission p : permissions) {
        while (!p.enabled && p.impliedBy != null)
          p = p.impliedBy; 
        enabledPermissions.add(p);
      } 
      String permissionsDisplayName = (String)enabledPermissions.stream().map(p -> "" + p.group.title + "/" + p.group.title).collect(Collectors.joining(", "));
      if (enabledPermissions.size() == 1) {
        errorMessage = Messages.AccessDeniedException2_MissingPermission(authentication.getName(), permissionsDisplayName);
      } else {
        errorMessage = Messages.AccessDeniedException_MissingPermissions(authentication.getName(), permissionsDisplayName);
      } 
      throw new AccessDeniedException(errorMessage);
    } 
  }
  
  public final boolean hasPermission(@NonNull Permission p) {
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(SYSTEM2))
      return true; 
    return hasPermission2(a, p);
  }
  
  public final boolean hasAnyPermission(@NonNull Permission... permissions) {
    if (permissions.length == 0)
      throw new IllegalArgumentException("At least one permission must be provided"); 
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(SYSTEM2))
      return true; 
    for (Permission permission : permissions) {
      if (hasPermission(permission))
        return true; 
    } 
    return false;
  }
  
  public boolean hasPermission2(@NonNull Authentication a, @NonNull Permission permission) {
    if (Util.isOverridden(ACL.class, getClass(), "hasPermission", new Class[] { Authentication.class, Permission.class }))
      return hasPermission(Authentication.fromSpring(a), permission); 
    throw new AbstractMethodError("implement hasPermission2");
  }
  
  @Deprecated
  public boolean hasPermission(@NonNull Authentication a, @NonNull Permission permission) { return hasPermission2(a.toSpring(), permission); }
  
  public static ACL lambda2(BiFunction<Authentication, Permission, Boolean> impl) { return new Object(impl); }
  
  @Deprecated
  public static ACL lambda(BiFunction<Authentication, Permission, Boolean> impl) { return new Object(impl); }
  
  public final void checkCreatePermission(@NonNull ItemGroup c, @NonNull TopLevelItemDescriptor d) {
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(SYSTEM2))
      return; 
    if (!hasCreatePermission2(a, c, d))
      throw new AccessDeniedException(Messages.AccessDeniedException2_MissingPermission(a.getName(), "" + Item.CREATE.group.title + "/" + Item.CREATE.group.title + Item.CREATE.name + "/" + Item.CREATE)); 
  }
  
  public boolean hasCreatePermission2(@NonNull Authentication a, @NonNull ItemGroup c, @NonNull TopLevelItemDescriptor d) {
    if (Util.isOverridden(ACL.class, getClass(), "hasCreatePermission", new Class[] { Authentication.class, ItemGroup.class, TopLevelItemDescriptor.class }))
      return hasCreatePermission(Authentication.fromSpring(a), c, d); 
    return true;
  }
  
  @Deprecated
  public boolean hasCreatePermission(@NonNull Authentication a, @NonNull ItemGroup c, @NonNull TopLevelItemDescriptor d) { return hasCreatePermission2(a.toSpring(), c, d); }
  
  public final void checkCreatePermission(@NonNull ViewGroup c, @NonNull ViewDescriptor d) {
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(SYSTEM2))
      return; 
    if (!hasCreatePermission2(a, c, d))
      throw new AccessDeniedException(Messages.AccessDeniedException2_MissingPermission(a.getName(), "" + View.CREATE.group.title + "/" + View.CREATE.group.title + View.CREATE.name + "/" + View.CREATE)); 
  }
  
  public boolean hasCreatePermission2(@NonNull Authentication a, @NonNull ViewGroup c, @NonNull ViewDescriptor d) {
    if (Util.isOverridden(ACL.class, getClass(), "hasCreatePermission", new Class[] { Authentication.class, ViewGroup.class, ViewDescriptor.class }))
      return hasCreatePermission(Authentication.fromSpring(a), c, d); 
    return true;
  }
  
  @Deprecated
  public boolean hasCreatePermission(@NonNull Authentication a, @NonNull ViewGroup c, @NonNull ViewDescriptor d) { return hasCreatePermission2(a.toSpring(), c, d); }
  
  public static final Sid EVERYONE = new Object();
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String ANONYMOUS_USERNAME = "anonymous";
  
  public static final Sid ANONYMOUS = new PrincipalSid("anonymous");
  
  static final Sid[] AUTOMATIC_SIDS = { EVERYONE, ANONYMOUS };
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String SYSTEM_USERNAME = "SYSTEM";
  
  public static final Authentication SYSTEM2 = new UsernamePasswordAuthenticationToken("SYSTEM", "SYSTEM");
  
  @Deprecated
  public static final Authentication SYSTEM = new UsernamePasswordAuthenticationToken((UsernamePasswordAuthenticationToken)SYSTEM2);
  
  @Deprecated
  @NonNull
  public static SecurityContext impersonate2(@NonNull Authentication auth) {
    SecurityContext old = SecurityContextHolder.getContext();
    SecurityContextHolder.setContext(new NonSerializableSecurityContext(auth));
    return old;
  }
  
  @Deprecated
  @NonNull
  public static SecurityContext impersonate(@NonNull Authentication auth) { return SecurityContext.fromSpring(impersonate2(auth.toSpring())); }
  
  @Deprecated
  public static void impersonate2(@NonNull Authentication auth, @NonNull Runnable body) {
    old = impersonate2(auth);
    try {
      body.run();
    } finally {
      SecurityContextHolder.setContext(old);
    } 
  }
  
  @Deprecated
  public static void impersonate(@NonNull Authentication auth, @NonNull Runnable body) { impersonate2(auth.toSpring(), body); }
  
  @Deprecated
  public static <V, T extends Exception> V impersonate2(Authentication auth, Callable<V, T> body) throws T {
    old = impersonate2(auth);
    try {
      object = body.call();
      return (V)object;
    } finally {
      SecurityContextHolder.setContext(old);
    } 
  }
  
  @Deprecated
  public static <V, T extends Exception> V impersonate(Authentication auth, Callable<V, T> body) throws T { return (V)impersonate2(auth.toSpring(), body); }
  
  @NonNull
  public static ACLContext as2(@NonNull Authentication auth) {
    ACLContext context = new ACLContext(SecurityContextHolder.getContext());
    SecurityContextHolder.setContext(new NonSerializableSecurityContext(auth));
    return context;
  }
  
  @Deprecated
  @NonNull
  public static ACLContext as(@NonNull Authentication auth) { return as2(auth.toSpring()); }
  
  @NonNull
  public static ACLContext as(@CheckForNull User user) { return as2((user == null) ? Jenkins.ANONYMOUS2 : user.impersonate2()); }
  
  public static boolean isAnonymous2(@NonNull Authentication authentication) { return authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken; }
  
  @Deprecated
  public static boolean isAnonymous(@NonNull Authentication authentication) { return isAnonymous2(authentication.toSpring()); }
}
