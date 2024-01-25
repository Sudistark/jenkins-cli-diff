package hudson.search;

import hudson.model.Item;
import hudson.model.ItemGroup;

public class SuggestedItem {
  private final SuggestedItem parent;
  
  public final SearchItem item;
  
  private String path;
  
  public SuggestedItem(SearchItem top) { this(null, top); }
  
  public SuggestedItem(SuggestedItem parent, SearchItem item) {
    this.parent = parent;
    this.item = item;
  }
  
  public String getPath() {
    if (this.path != null)
      return this.path; 
    if (this.parent == null)
      return this.path = this.item.getSearchName(); 
    StringBuilder buf = new StringBuilder();
    getPath(buf);
    return this.path = buf.toString();
  }
  
  private void getPath(StringBuilder buf) {
    if (this.parent == null) {
      buf.append(this.item.getSearchName());
    } else {
      this.parent.getPath(buf);
      buf.append(' ').append(this.item.getSearchName());
    } 
  }
  
  public String getUrl() {
    StringBuilder buf = new StringBuilder();
    getUrl(buf);
    return buf.toString();
  }
  
  private static SuggestedItem build(SearchableModelObject searchContext, Item top) {
    ItemGroup<? extends Item> parent = top.getParent();
    if (parent instanceof Item) {
      Item parentItem = (Item)parent;
      return new SuggestedItem(build(searchContext, parentItem), top);
    } 
    return new SuggestedItem(top);
  }
  
  public static SuggestedItem build(SearchableModelObject searchContext, SearchItem si) {
    if (si instanceof Item)
      return build(searchContext, (Item)si); 
    return new SuggestedItem(si);
  }
  
  private void getUrl(StringBuilder buf) {
    if (this.parent != null)
      this.parent.getUrl(buf); 
    String f = this.item.getSearchUrl();
    if (f.startsWith("/")) {
      buf.setLength(0);
      buf.append(f);
    } else {
      if (buf.length() == 0 || buf.charAt(buf.length() - 1) != '/')
        buf.append('/'); 
      buf.append(f);
    } 
  }
}
