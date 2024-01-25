package hudson.search;

import hudson.model.ModelObject;

public interface SearchableModelObject extends ModelObject, SearchItem {
  Search getSearch();
}
