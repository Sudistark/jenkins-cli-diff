package hudson.search;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class CollectionSearchIndex<SMT extends SearchableModelObject> extends Object implements SearchIndex {
  protected abstract SearchItem get(String paramString);
  
  protected abstract Collection<SMT> all();
  
  @NonNull
  protected Iterable<SMT> allAsIterable() {
    Collection<SMT> all = all();
    return (all == null) ? Collections.emptySet() : all;
  }
  
  public void find(String token, List<SearchItem> result) {
    SearchItem p = get(token);
    if (p != null)
      result.add(p); 
  }
  
  public void suggest(String token, List<SearchItem> result) {
    boolean isCaseSensitive = UserSearchProperty.isCaseInsensitive();
    if (isCaseSensitive)
      token = token.toLowerCase(); 
    for (Iterator iterator = allAsIterable().iterator(); iterator.hasNext(); ) {
      SMT o = (SMT)(SearchableModelObject)iterator.next();
      String name = getName(o);
      if (isCaseSensitive)
        name = name.toLowerCase(); 
      if (o != null && name.contains(token))
        result.add(o); 
    } 
  }
  
  protected String getName(SMT o) { return o.getDisplayName(); }
}
