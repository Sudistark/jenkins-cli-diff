package hudson.search;

import java.util.List;

public interface SearchIndex {
  public static final SearchIndex EMPTY = new Object();
  
  void find(String paramString, List<SearchItem> paramList);
  
  void suggest(String paramString, List<SearchItem> paramList);
}
