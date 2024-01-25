package hudson.util;

import java.io.Serializable;
import java.util.Comparator;

@Deprecated
public final class CaseInsensitiveComparator extends Object implements Comparator<String>, Serializable {
  public static final Comparator<String> INSTANCE = new CaseInsensitiveComparator();
  
  private static final long serialVersionUID = 1L;
  
  public int compare(String lhs, String rhs) { return lhs.compareToIgnoreCase(rhs); }
  
  private Object readResolve() { return INSTANCE; }
}
