package jenkins.util;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public final class TreeString implements Serializable {
  private static final long serialVersionUID = 3621959682117480904L;
  
  private TreeString parent;
  
  private char[] label;
  
  TreeString() { this(null, ""); }
  
  TreeString(TreeString parent, String label) {
    assert parent == null || label.length() > 0;
    this.parent = parent;
    this.label = label.toCharArray();
  }
  
  String getLabel() { return new String(this.label); }
  
  TreeString split(String prefix) {
    assert getLabel().startsWith(prefix);
    char[] suffix = new char[this.label.length - prefix.length()];
    System.arraycopy(this.label, prefix.length(), suffix, 0, suffix.length);
    TreeString middle = new TreeString(this.parent, prefix);
    this.label = suffix;
    this.parent = middle;
    return middle;
  }
  
  private int depth() {
    int i = 0;
    for (TreeString p = this; p != null; p = p.parent)
      i++; 
    return i;
  }
  
  public boolean equals(Object rhs) {
    if (rhs == null)
      return false; 
    return (rhs.getClass() == TreeString.class && ((TreeString)rhs)
      .getLabel().equals(getLabel()));
  }
  
  public int hashCode() {
    int h = (this.parent == null) ? 0 : this.parent.hashCode();
    for (char c : this.label)
      h = 31 * h + c; 
    assert toString().hashCode() == h;
    return h;
  }
  
  public String toString() {
    char[][] tokens = new char[depth()][];
    int i = tokens.length;
    int sz = 0;
    for (TreeString p = this; p != null; p = p.parent) {
      tokens[--i] = p.label;
      sz += p.label.length;
    } 
    StringBuilder buf = new StringBuilder(sz);
    for (char[] token : tokens)
      buf.append(token); 
    return buf.toString();
  }
  
  void dedup(Map<String, char[]> table) {
    String l = getLabel();
    char[] v = (char[])table.get(l);
    if (v != null) {
      this.label = v;
    } else {
      table.put(l, this.label);
    } 
  }
  
  public boolean isBlank() { return StringUtils.isBlank(toString()); }
  
  public static String toString(TreeString t) { return (t == null) ? null : t.toString(); }
  
  public static TreeString of(String s) {
    if (s == null)
      return null; 
    return new TreeString(null, s);
  }
}
