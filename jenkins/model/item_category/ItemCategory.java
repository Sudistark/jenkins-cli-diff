package jenkins.model.item_category;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.RestrictedSince;
import hudson.model.TopLevelItemDescriptor;
import org.kohsuke.accmod.Restricted;

public abstract class ItemCategory implements ExtensionPoint {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.14")
  public static final int MIN_TOSHOW = 1;
  
  private int order = 1;
  
  public abstract String getId();
  
  public abstract String getDescription();
  
  public abstract String getDisplayName();
  
  public abstract int getMinToShow();
  
  private void setOrder(int order) { this.order = order; }
  
  public int getOrder() { return this.order; }
  
  @NonNull
  public static ItemCategory getCategory(TopLevelItemDescriptor descriptor) {
    int order = 0;
    ExtensionList<ItemCategory> categories = ExtensionList.lookup(ItemCategory.class);
    for (ItemCategory category : categories) {
      if (category.getId().equals(descriptor.getCategoryId())) {
        category.setOrder(++order);
        return category;
      } 
      order++;
    } 
    return new UncategorizedCategory();
  }
}
