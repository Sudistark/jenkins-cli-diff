package jenkins.model.lazy;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BuildReference<R> extends Object {
  private static final Logger LOGGER = Logger.getLogger(BuildReference.class.getName());
  
  final String id;
  
  public BuildReference(String id, R referent) {
    this.id = id;
    this.holder = findHolder(referent);
  }
  
  @CheckForNull
  public R get() {
    Holder<R> h = this.holder;
    return (R)((h != null) ? h.get() : null);
  }
  
  void clear() { this.holder = null; }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    BuildReference<?> that = (BuildReference)o;
    return this.id.equals(that.id);
  }
  
  public int hashCode() { return this.id.hashCode(); }
  
  public String toString() {
    R r = (R)get();
    return (r != null) ? r.toString() : this.id;
  }
  
  private static <R> Holder<R> findHolder(R referent) {
    if (referent == null)
      return new DefaultHolderFactory.NoHolder(); 
    for (HolderFactory f : ExtensionList.lookup(HolderFactory.class)) {
      Holder<R> h = f.make(referent);
      if (h != null) {
        LOGGER.log(Level.FINE, "created build reference for {0} using {1}", new Object[] { referent, f });
        return h;
      } 
    } 
    return (new DefaultHolderFactory()).make(referent);
  }
}
