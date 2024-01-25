package hudson.search;

public interface SearchItem {
  String getSearchName();
  
  String getSearchUrl();
  
  SearchIndex getSearchIndex();
}
