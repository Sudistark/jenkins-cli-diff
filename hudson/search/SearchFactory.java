package hudson.search;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class SearchFactory implements ExtensionPoint {
  public abstract Search createFor(SearchableModelObject paramSearchableModelObject);
  
  public static ExtensionList<SearchFactory> all() { return ExtensionList.lookup(SearchFactory.class); }
}
