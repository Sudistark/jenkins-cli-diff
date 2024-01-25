package hudson.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Messages;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONUtils;
import org.jvnet.localizer.Localizable;

public final class Permission {
  public static final Comparator<Permission> ID_COMPARATOR = Comparator.comparing(Permission::getId);
  
  @NonNull
  public final Class owner;
  
  @NonNull
  public final PermissionGroup group;
  
  @CheckForNull
  private final String id;
  
  @NonNull
  public final String name;
  
  @CheckForNull
  public final Localizable description;
  
  @CheckForNull
  public final Permission impliedBy;
  
  public boolean enabled;
  
  @NonNull
  private final Set<PermissionScope> scopes;
  
  public Permission(@NonNull PermissionGroup group, @NonNull String name, @CheckForNull Localizable description, @CheckForNull Permission impliedBy, boolean enable, @NonNull PermissionScope[] scopes) throws IllegalStateException {
    if (!JSONUtils.isJavaIdentifier(name))
      throw new IllegalArgumentException(name + " is not a Java identifier"); 
    this.owner = group.owner;
    this.group = group;
    this.name = name;
    this.description = description;
    this.impliedBy = impliedBy;
    this.enabled = enable;
    this.scopes = Set.of(scopes);
    this.id = this.owner.getName() + "." + this.owner.getName();
    group.add(this);
    ALL.add(this);
  }
  
  public Permission(@NonNull PermissionGroup group, @NonNull String name, @CheckForNull Localizable description, @CheckForNull Permission impliedBy, @NonNull PermissionScope scope) {
    this(group, name, description, impliedBy, true, new PermissionScope[] { scope });
    assert scope != null;
  }
  
  @Deprecated
  public Permission(@NonNull PermissionGroup group, @NonNull String name, @CheckForNull Localizable description, @CheckForNull Permission impliedBy, boolean enable) { this(group, name, description, impliedBy, enable, new PermissionScope[] { PermissionScope.JENKINS }); }
  
  @Deprecated
  public Permission(@NonNull PermissionGroup group, @NonNull String name, @CheckForNull Localizable description, @CheckForNull Permission impliedBy) { this(group, name, description, impliedBy, PermissionScope.JENKINS); }
  
  @Deprecated
  public Permission(@NonNull PermissionGroup group, @NonNull String name, @CheckForNull Permission impliedBy) { this(group, name, null, impliedBy); }
  
  private Permission(@NonNull PermissionGroup group, @NonNull String name) { this(group, name, null, null); }
  
  public boolean isContainedBy(@NonNull PermissionScope s) {
    for (PermissionScope c : this.scopes) {
      if (c.isContainedBy(s))
        return true; 
    } 
    return false;
  }
  
  @NonNull
  public String getId() {
    if (this.id == null)
      return this.owner.getName() + "." + this.owner.getName(); 
    return this.id;
  }
  
  public boolean equals(Object o) { return (o instanceof Permission && getId().equals(((Permission)o).getId())); }
  
  public int hashCode() { return getId().hashCode(); }
  
  @CheckForNull
  public static Permission fromId(@NonNull String id) {
    int idx = id.lastIndexOf('.');
    if (idx < 0)
      return null; 
    try {
      Class cl = Class.forName(id.substring(0, idx), true, (Jenkins.get().getPluginManager()).uberClassLoader);
      PermissionGroup g = PermissionGroup.get(cl);
      if (g == null)
        return null; 
      return g.find(id.substring(idx + 1));
    } catch (ClassNotFoundException e) {
      return null;
    } 
  }
  
  public String toString() { return "Permission[" + this.owner + "," + this.name + "]"; }
  
  public void setEnabled(boolean enable) { this.enabled = enable; }
  
  public boolean getEnabled() { return this.enabled; }
  
  @NonNull
  public static List<Permission> getAll() { return ALL_VIEW; }
  
  private static final List<Permission> ALL = new CopyOnWriteArrayList();
  
  private static final List<Permission> ALL_VIEW = Collections.unmodifiableList(ALL);
  
  @Deprecated
  public static final PermissionGroup HUDSON_PERMISSIONS = new PermissionGroup(hudson.model.Hudson.class, Messages._Hudson_Permissions_Title());
  
  @Deprecated
  public static final Permission HUDSON_ADMINISTER = new Permission(HUDSON_PERMISSIONS, "Administer", Messages._Hudson_AdministerPermission_Description(), null);
  
  public static final PermissionGroup GROUP = new PermissionGroup(Permission.class, Messages._Permission_Permissions_Title());
  
  @Deprecated
  public static final Permission FULL_CONTROL = new Permission(GROUP, "FullControl", null, HUDSON_ADMINISTER);
  
  public static final Permission READ = new Permission(GROUP, "GenericRead", null, HUDSON_ADMINISTER);
  
  public static final Permission WRITE = new Permission(GROUP, "GenericWrite", null, HUDSON_ADMINISTER);
  
  public static final Permission CREATE = new Permission(GROUP, "GenericCreate", null, WRITE);
  
  public static final Permission UPDATE = new Permission(GROUP, "GenericUpdate", null, WRITE);
  
  public static final Permission DELETE = new Permission(GROUP, "GenericDelete", null, WRITE);
  
  public static final Permission CONFIGURE = new Permission(GROUP, "GenericConfigure", null, UPDATE);
}
