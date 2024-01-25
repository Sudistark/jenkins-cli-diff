package hudson.search;

public class SearchItems {
  public static SearchItem create(String searchName, String url) { return create(searchName, url, SearchIndex.EMPTY); }
  
  public static SearchItem create(String searchName, String url, SearchIndex children) { return new Object(searchName, url, children); }
  
  public static SearchItem create(String searchName, String url, SearchableModelObject searchable) { return new Object(searchName, url, searchable); }
}
