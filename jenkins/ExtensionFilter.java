package jenkins;

import hudson.ExtensionComponent;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class ExtensionFilter implements ExtensionPoint {
  public abstract <T> boolean allows(Class<T> paramClass, ExtensionComponent<T> paramExtensionComponent);
  
  public static <T> boolean isAllowed(Class<T> type, ExtensionComponent<T> component) {
    if (type == ExtensionFilter.class || type == hudson.ExtensionFinder.class)
      return true; 
    for (ExtensionFilter f : all()) {
      if (!f.allows(type, component))
        return false; 
    } 
    return true;
  }
  
  public static ExtensionList<ExtensionFilter> all() { return ExtensionList.lookup(ExtensionFilter.class); }
}
