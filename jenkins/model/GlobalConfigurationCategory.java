package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.ModelObject;

public abstract class GlobalConfigurationCategory implements ExtensionPoint, ModelObject {
  public abstract String getShortDescription();
  
  public static ExtensionList<GlobalConfigurationCategory> all() { return ExtensionList.lookup(GlobalConfigurationCategory.class); }
  
  @NonNull
  public static <T extends GlobalConfigurationCategory> T get(Class<T> type) {
    T category = (T)(GlobalConfigurationCategory)all().get(type);
    if (category == null)
      throw new AssertionError("Category not found. It seems the " + type + " is not annotated with @Extension and so not registered"); 
    return category;
  }
}
