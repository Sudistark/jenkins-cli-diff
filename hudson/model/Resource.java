package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public final class Resource {
  @NonNull
  public final String displayName;
  
  @CheckForNull
  public final Resource parent;
  
  public final int numConcurrentWrite;
  
  public Resource(@CheckForNull Resource parent, @NonNull String displayName) { this(parent, displayName, 1); }
  
  public Resource(@CheckForNull Resource parent, @NonNull String displayName, int numConcurrentWrite) {
    if (numConcurrentWrite < 1)
      throw new IllegalArgumentException(); 
    this.parent = parent;
    this.displayName = displayName;
    this.numConcurrentWrite = numConcurrentWrite;
  }
  
  public Resource(@NonNull String displayName) { this(null, displayName); }
  
  public boolean isCollidingWith(Resource that, int count) {
    assert that != null;
    for (Resource r = that; r != null; r = r.parent) {
      if (equals(r) && r.numConcurrentWrite < count)
        return true; 
    } 
    for (Resource r = this; r != null; r = r.parent) {
      if (that.equals(r) && r.numConcurrentWrite < count)
        return true; 
    } 
    return false;
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    Resource that = (Resource)o;
    return (this.displayName.equals(that.displayName) && eq(this.parent, that.parent));
  }
  
  private static boolean eq(Object lhs, Object rhs) {
    if (lhs == rhs)
      return true; 
    if (lhs == null || rhs == null)
      return false; 
    return lhs.equals(rhs);
  }
  
  public int hashCode() { return this.displayName.hashCode(); }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (this.parent != null)
      buf.append(this.parent).append('/'); 
    buf.append(this.displayName).append('(').append(this.numConcurrentWrite).append(')');
    return buf.toString();
  }
}
