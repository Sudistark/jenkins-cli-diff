package hudson.security;

import hudson.model.ModelObject;
import java.util.Set;

public final class PermissionScope {
  public final Class<? extends ModelObject> modelClass;
  
  private final Set<PermissionScope> containers;
  
  public PermissionScope(Class<? extends ModelObject> modelClass, PermissionScope... containers) {
    this.modelClass = modelClass;
    this.containers = Set.of(containers);
  }
  
  public boolean isContainedBy(PermissionScope s) {
    if (this == s)
      return true; 
    for (PermissionScope c : this.containers) {
      if (c.isContainedBy(s))
        return true; 
    } 
    return false;
  }
  
  public static final PermissionScope JENKINS = new PermissionScope(jenkins.model.Jenkins.class, new PermissionScope[0]);
  
  public static final PermissionScope ITEM_GROUP = new PermissionScope(hudson.model.ItemGroup.class, new PermissionScope[] { JENKINS });
  
  public static final PermissionScope ITEM = new PermissionScope(hudson.model.Item.class, new PermissionScope[] { ITEM_GROUP });
  
  public static final PermissionScope RUN = new PermissionScope(hudson.model.Run.class, new PermissionScope[] { ITEM });
  
  public static final PermissionScope COMPUTER = new PermissionScope(hudson.model.Computer.class, new PermissionScope[] { JENKINS });
}
