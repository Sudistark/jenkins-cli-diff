package hudson.search;

import java.util.List;

public class UnionSearchIndex implements SearchIndex {
  private final SearchIndex lhs;
  
  private final SearchIndex rhs;
  
  public static SearchIndex combine(SearchIndex... sets) {
    UnionSearchIndex unionSearchIndex = EMPTY;
    for (SearchIndex q : sets) {
      if (q != null && q != EMPTY)
        if (unionSearchIndex == EMPTY) {
          unionSearchIndex = q;
        } else {
          unionSearchIndex = new UnionSearchIndex(unionSearchIndex, q);
        }  
    } 
    return unionSearchIndex;
  }
  
  public UnionSearchIndex(SearchIndex lhs, SearchIndex rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void find(String token, List<SearchItem> result) {
    this.lhs.find(token, result);
    this.rhs.find(token, result);
  }
  
  public void suggest(String token, List<SearchItem> result) {
    this.lhs.suggest(token, result);
    this.rhs.suggest(token, result);
  }
}
