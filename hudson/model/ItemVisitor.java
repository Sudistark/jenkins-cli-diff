package hudson.model;

import jenkins.model.Jenkins;

public abstract class ItemVisitor {
  public void onItemGroup(ItemGroup<?> group) {
    for (Item i : group.getItems()) {
      if (i.hasPermission(Item.READ))
        onItem(i); 
    } 
  }
  
  public void onItem(Item i) {
    if (i instanceof ItemGroup)
      onItemGroup((ItemGroup)i); 
  }
  
  public final void walk() { onItemGroup(Jenkins.get()); }
}
