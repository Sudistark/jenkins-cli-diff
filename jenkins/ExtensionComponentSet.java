package jenkins;

import hudson.ExtensionComponent;
import hudson.ExtensionFinder;
import java.util.Arrays;
import java.util.Collection;

public abstract class ExtensionComponentSet {
  public abstract <T> Collection<ExtensionComponent<T>> find(Class<T> paramClass);
  
  public final ExtensionComponentSet filtered() {
    ExtensionComponentSet base = this;
    return new Object(this, base);
  }
  
  public static final ExtensionComponentSet EMPTY = new Object();
  
  public static ExtensionComponentSet union(Collection<? extends ExtensionComponentSet> base) { return new Object(base); }
  
  public static ExtensionComponentSet union(ExtensionComponentSet... members) { return union(Arrays.asList(members)); }
  
  public static ExtensionComponentSet allOf(ExtensionFinder f) { return new Object(f); }
}
