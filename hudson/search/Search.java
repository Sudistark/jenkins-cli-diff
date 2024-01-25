package hudson.search;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.DataWriter;
import org.kohsuke.stapler.export.Flavor;

public class Search implements StaplerProxy {
  private static int MAX_SEARCH_SIZE = Integer.getInteger(Search.class.getName() + ".MAX_SEARCH_SIZE", 500).intValue();
  
  public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    List<Ancestor> l = req.getAncestors();
    for (int i = l.size() - 1; i >= 0; i--) {
      Ancestor a = (Ancestor)l.get(i);
      if (a.getObject() instanceof SearchableModelObject) {
        SearchableModelObject smo = (SearchableModelObject)a.getObject();
        if (LOGGER.isLoggable(Level.FINE))
          LOGGER.fine(String.format("smo.displayName=%s, searchName=%s", new Object[] { smo.getDisplayName(), smo.getSearchName() })); 
        SearchIndex index = smo.getSearchIndex();
        String query = req.getParameter("q");
        if (query != null) {
          SuggestedItem target = find(index, query, smo);
          if (target != null) {
            rsp.sendRedirect2(req.getContextPath() + req.getContextPath());
            return;
          } 
        } 
      } 
    } 
    rsp.setStatus(404);
    req.getView(this, "search-failed.jelly").forward(req, rsp);
  }
  
  public void doSuggestOpenSearch(StaplerRequest req, StaplerResponse rsp, @QueryParameter String q) throws IOException, ServletException {
    rsp.setContentType(Flavor.JSON.contentType);
    DataWriter w = Flavor.JSON.createDataWriter(null, rsp);
    w.startArray();
    w.value(q);
    w.startArray();
    for (SuggestedItem item : getSuggestions(req, q))
      w.value(item.getPath()); 
    w.endArray();
    w.endArray();
  }
  
  public void doSuggest(StaplerRequest req, StaplerResponse rsp, @QueryParameter String query) throws IOException, ServletException {
    Result r = new Result();
    for (SuggestedItem item : getSuggestions(req, query))
      r.suggestions.add(new Item(item.getPath())); 
    rsp.serveExposedBean(req, r, Flavor.JSON);
  }
  
  public SearchResult getSuggestions(StaplerRequest req, String query) {
    Set<String> paths = new HashSet<String>();
    SearchResultImpl r = new SearchResultImpl();
    int max = Math.min(
        req.hasParameter("max") ? Integer.parseInt(req.getParameter("max")) : 100, MAX_SEARCH_SIZE);
    SearchableModelObject smo = findClosestSearchableModelObject(req);
    for (SuggestedItem i : suggest(makeSuggestIndex(req), query, smo)) {
      if (r.size() >= max) {
        r.hasMoreResults = true;
        break;
      } 
      if (paths.add(i.getPath()))
        r.add(i); 
    } 
    return r;
  }
  
  public int getMaxSearchSize() { return MAX_SEARCH_SIZE; }
  
  @CheckForNull
  private SearchableModelObject findClosestSearchableModelObject(StaplerRequest req) {
    List<Ancestor> l = req.getAncestors();
    for (int i = l.size() - 1; i >= 0; i--) {
      Ancestor a = (Ancestor)l.get(i);
      if (a.getObject() instanceof SearchableModelObject)
        return (SearchableModelObject)a.getObject(); 
    } 
    return null;
  }
  
  private SearchIndex makeSuggestIndex(StaplerRequest req) {
    SearchIndexBuilder builder = new SearchIndexBuilder();
    for (Ancestor a : req.getAncestors()) {
      if (a.getObject() instanceof SearchableModelObject) {
        SearchableModelObject smo = (SearchableModelObject)a.getObject();
        builder.add(smo.getSearchIndex());
      } 
    } 
    return builder.make();
  }
  
  static SuggestedItem findClosestSuggestedItem(List<SuggestedItem> r, String query) {
    for (SuggestedItem curItem : r) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine(String.format("item's searchUrl:%s;query=%s", new Object[] { curItem.item.getSearchUrl(), query })); 
      if (curItem.item.getSearchUrl().contains(Util.rawEncode(query)))
        return curItem; 
    } 
    return (SuggestedItem)r.get(0);
  }
  
  @Deprecated
  public static SuggestedItem find(SearchIndex index, String query) { return find(index, query, null); }
  
  public static SuggestedItem find(SearchIndex index, String query, SearchableModelObject searchContext) {
    List<SuggestedItem> r = find(Mode.FIND, index, query, searchContext);
    if (r.isEmpty())
      return null; 
    if (1 == r.size())
      return (SuggestedItem)r.get(0); 
    return findClosestSuggestedItem(r, query);
  }
  
  @Deprecated
  public static List<SuggestedItem> suggest(SearchIndex index, String tokenList) { return suggest(index, tokenList, null); }
  
  public static List<SuggestedItem> suggest(SearchIndex index, String tokenList, SearchableModelObject searchContext) {
    List<Tag> buf = new ArrayList<Tag>();
    List<SuggestedItem> items = find(Mode.SUGGEST, index, tokenList, searchContext);
    for (SuggestedItem i : items)
      buf.add(new Tag(i, tokenList)); 
    Collections.sort(buf);
    items.clear();
    for (Tag t : buf)
      items.add(t.item); 
    return items;
  }
  
  private static List<SuggestedItem> find(Mode m, SearchIndex index, String tokenList, SearchableModelObject searchContext) {
    TokenList tokens = new TokenList(tokenList);
    if (tokens.length() == 0)
      return Collections.emptyList(); 
    List[] paths = new List[tokens.length() + 1];
    for (int i = 1; i <= tokens.length(); i++)
      paths[i] = new ArrayList(); 
    List<SearchItem> items = new ArrayList<SearchItem>();
    LOGGER.log(Level.FINE, "tokens={0}", tokens);
    int w = 1;
    for (String token : tokens.subSequence(0)) {
      items.clear();
      m.find(index, token, items);
      for (SearchItem si : items) {
        paths[w].add(SuggestedItem.build(searchContext, si));
        LOGGER.log(Level.FINE, "found search item: {0}", si.getSearchName());
      } 
      w++;
    } 
    for (int j = 1; j < tokens.length(); j++) {
      w = 1;
      for (String token : tokens.subSequence(j)) {
        for (SuggestedItem r : paths[j]) {
          items.clear();
          m.find(r.item.getSearchIndex(), token, items);
          for (SearchItem i : items)
            paths[j + w].add(new SuggestedItem(r, i)); 
        } 
        w++;
      } 
    } 
    return paths[tokens.length()];
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.READ); 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(Search.class.getName() + ".skipPermissionCheck");
  
  private static final Logger LOGGER = Logger.getLogger(Search.class.getName());
}
