package hudson.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jvnet.localizer.Localizable;

public final class PermissionGroup extends Object implements Iterable<Permission>, Comparable<PermissionGroup> {
  private final SortedSet<Permission> permissions;
  
  @NonNull
  public final Class owner;
  
  public final Localizable title;
  
  private final String id;
  
  public PermissionGroup(@NonNull Class owner, Localizable title) throws IllegalStateException { this(title.toString(Locale.ENGLISH), owner, title); }
  
  public PermissionGroup(String id, @NonNull Class owner, Localizable title) throws IllegalStateException {
    this.permissions = new TreeSet(Permission.ID_COMPARATOR);
    this.owner = owner;
    this.title = title;
    this.id = id;
    register(this);
  }
  
  public String getId() { return this.id; }
  
  public String getOwnerClassName() { return this.owner.getName(); }
  
  public Iterator<Permission> iterator() { return getPermissions().iterator(); }
  
  void add(Permission p) {
    if (!this.permissions.add(p))
      throw new IllegalStateException("attempt to register a second Permission for " + p.getId()); 
  }
  
  public List<Permission> getPermissions() { return new ArrayList(this.permissions); }
  
  public boolean hasPermissionContainedBy(PermissionScope scope) {
    for (Permission p : this.permissions) {
      if (p.isContainedBy(scope))
        return true; 
    } 
    return false;
  }
  
  public Permission find(String name) {
    for (Permission p : this.permissions) {
      if (p.name.equals(name))
        return p; 
    } 
    return null;
  }
  
  public int compareTo(PermissionGroup that) {
    int r = compareOrder() - that.compareOrder();
    if (r != 0)
      return r; 
    return getOwnerClassName().compareTo(that.getOwnerClassName());
  }
  
  private int compareOrder() {
    if (this.owner == hudson.model.Hudson.class)
      return 0; 
    return 1;
  }
  
  public boolean equals(Object o) { return (o instanceof PermissionGroup && getOwnerClassName().equals(((PermissionGroup)o).getOwnerClassName())); }
  
  public int hashCode() { return getOwnerClassName().hashCode(); }
  
  public int size() { return this.permissions.size(); }
  
  public String toString() { return "PermissionGroup[" + getOwnerClassName() + "]"; }
  
  private static void register(PermissionGroup g) {
    if (!PERMISSIONS.add(g))
      throw new IllegalStateException("attempt to register a second PermissionGroup for " + g.getOwnerClassName()); 
  }
  
  public static List<PermissionGroup> getAll() { return new ArrayList(PERMISSIONS); }
  
  @CheckForNull
  public static PermissionGroup get(Class owner) {
    for (PermissionGroup g : PERMISSIONS) {
      if (g.owner == owner)
        return g; 
    } 
    return null;
  }
  
  private static final SortedSet<PermissionGroup> PERMISSIONS = new TreeSet();
}
