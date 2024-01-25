package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Functions;
import hudson.Util;
import hudson.search.SearchableModelObject;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import java.io.IOException;
import java.util.Collection;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.io.OnMaster;

public interface Item extends PersistenceRoot, SearchableModelObject, AccessControlled, OnMaster {
  ItemGroup<? extends Item> getParent();
  
  Collection<? extends Job> getAllJobs();
  
  String getName();
  
  String getFullName();
  
  String getDisplayName();
  
  String getFullDisplayName();
  
  @Nullable
  default String getRelativeNameFrom(@CheckForNull ItemGroup g) { return Functions.getRelativeNameFrom(this, g); }
  
  @Nullable
  default String getRelativeNameFrom(@NonNull Item item) { return getRelativeNameFrom(item.getParent()); }
  
  String getUrl();
  
  String getShortUrl();
  
  @Deprecated
  default String getAbsoluteUrl() {
    String r = Jenkins.get().getRootUrl();
    if (r == null)
      throw new IllegalStateException("Root URL isn't configured yet. Cannot compute absolute URL."); 
    return Util.encode(r + r);
  }
  
  void onLoad(ItemGroup<? extends Item> paramItemGroup, String paramString) throws IOException;
  
  void onCopiedFrom(Item paramItem);
  
  default void onCreatedFromScratch() {}
  
  public static final PermissionGroup PERMISSIONS = new PermissionGroup(Item.class, Messages._Item_Permissions_Title());
  
  public static final Permission CREATE = new Permission(PERMISSIONS, "Create", 


      
      Messages._Item_CREATE_description(), Permission.CREATE, PermissionScope.ITEM_GROUP);
  
  public static final Permission DELETE = new Permission(PERMISSIONS, "Delete", 


      
      Messages._Item_DELETE_description(), Permission.DELETE, PermissionScope.ITEM);
  
  public static final Permission CONFIGURE = new Permission(PERMISSIONS, "Configure", 


      
      Messages._Item_CONFIGURE_description(), Permission.CONFIGURE, PermissionScope.ITEM);
  
  public static final Permission READ = new Permission(PERMISSIONS, "Read", 


      
      Messages._Item_READ_description(), Permission.READ, PermissionScope.ITEM);
  
  public static final Permission DISCOVER = new Permission(PERMISSIONS, "Discover", 


      
      Messages._AbstractProject_DiscoverPermission_Description(), READ, PermissionScope.ITEM);
  
  public static final Permission EXTENDED_READ = new Permission(PERMISSIONS, "ExtendedRead", 


      
      Messages._AbstractProject_ExtendedReadPermission_Description(), CONFIGURE, 
      
      SystemProperties.getBoolean("hudson.security.ExtendedReadPermission"), new PermissionScope[] { PermissionScope.ITEM });
  
  public static final Permission BUILD = new Permission(PERMISSIONS, "Build", 


      
      Messages._AbstractProject_BuildPermission_Description(), Permission.UPDATE, PermissionScope.ITEM);
  
  public static final Permission WORKSPACE = new Permission(PERMISSIONS, "Workspace", 


      
      Messages._AbstractProject_WorkspacePermission_Description(), Permission.READ, PermissionScope.ITEM);
  
  public static final Permission WIPEOUT = new Permission(PERMISSIONS, "WipeOut", 


      
      Messages._AbstractProject_WipeOutPermission_Description(), null, 
      
      Functions.isWipeOutPermissionEnabled(), new PermissionScope[] { PermissionScope.ITEM });
  
  public static final Permission CANCEL = new Permission(PERMISSIONS, "Cancel", 


      
      Messages._AbstractProject_CancelPermission_Description(), Permission.UPDATE, PermissionScope.ITEM);
  
  void save();
  
  void delete();
}
