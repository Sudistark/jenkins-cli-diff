package jenkins.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TreeStringBuilder {
  Child root = new Child(new TreeString());
  
  public TreeString intern(String s) {
    if (s == null)
      return null; 
    return (this.root.intern(s)).node;
  }
  
  public TreeString intern(TreeString s) {
    if (s == null)
      return null; 
    return (this.root.intern(s.toString())).node;
  }
  
  public void dedup() { this.root.dedup(new HashMap()); }
  
  private static final Map<String, Child> NO_CHILDREN = Collections.emptyMap();
}
