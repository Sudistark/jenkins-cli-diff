package hudson.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class FixedSet implements SearchIndex {
  private final Collection<? extends SearchItem> items;
  
  public FixedSet(Collection<? extends SearchItem> items) { this.items = items; }
  
  public FixedSet(SearchItem... items) { this(Arrays.asList(items)); }
  
  public void find(String token, List<SearchItem> result) {
    boolean caseInsensitive = UserSearchProperty.isCaseInsensitive();
    for (SearchItem i : this.items) {
      String name = i.getSearchName();
      if (name != null && (name.equals(token) || (caseInsensitive && name.equalsIgnoreCase(token))))
        result.add(i); 
    } 
  }
  
  public void suggest(String token, List<SearchItem> result) {
    boolean caseInsensitive = UserSearchProperty.isCaseInsensitive();
    for (SearchItem i : this.items) {
      String name = i.getSearchName();
      if (name != null && (name.contains(token) || (caseInsensitive && StringUtils.containsIgnoreCase(name, token))))
        result.add(i); 
    } 
  }
}
