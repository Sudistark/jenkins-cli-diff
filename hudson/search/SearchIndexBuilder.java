package hudson.search;

import java.util.ArrayList;
import java.util.List;

public final class SearchIndexBuilder {
  private final List<SearchItem> items = new ArrayList();
  
  private final List<SearchIndex> indices = new ArrayList();
  
  public SearchIndexBuilder addAllAnnotations(SearchableModelObject o) {
    ParsedQuickSilver.get(o.getClass()).addTo(this, o);
    return this;
  }
  
  public SearchIndexBuilder add(String urlAsWellAsName) { return add(urlAsWellAsName, urlAsWellAsName); }
  
  public SearchIndexBuilder add(String url, String name) {
    this.items.add(SearchItems.create(name, url));
    return this;
  }
  
  public SearchIndexBuilder add(String url, String... names) {
    for (String name : names)
      add(url, name); 
    return this;
  }
  
  public SearchIndexBuilder add(SearchItem item) {
    this.items.add(item);
    return this;
  }
  
  public SearchIndexBuilder add(String url, SearchableModelObject searchable, String name) {
    this.items.add(SearchItems.create(name, url, searchable));
    return this;
  }
  
  public SearchIndexBuilder add(String url, SearchableModelObject searchable, String... names) {
    for (String name : names)
      add(url, searchable, name); 
    return this;
  }
  
  public SearchIndexBuilder add(SearchIndex index) {
    this.indices.add(index);
    return this;
  }
  
  public SearchIndexBuilder add(SearchIndexBuilder index) { return add(index.make()); }
  
  public SearchIndex make() {
    UnionSearchIndex unionSearchIndex = new FixedSet(this.items);
    for (SearchIndex index : this.indices)
      unionSearchIndex = new UnionSearchIndex(unionSearchIndex, index); 
    return unionSearchIndex;
  }
}
