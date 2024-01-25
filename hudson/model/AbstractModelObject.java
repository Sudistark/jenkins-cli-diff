package hudson.model;

import hudson.search.Search;
import hudson.search.SearchFactory;
import hudson.search.SearchIndex;
import hudson.search.SearchIndexBuilder;
import hudson.search.SearchableModelObject;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractModelObject implements SearchableModelObject {
  protected final void sendError(Exception e, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
    req.setAttribute("exception", e);
    sendError(e.getMessage(), req, rsp);
  }
  
  protected final void sendError(Exception e) throws ServletException, IOException { sendError(e, Stapler.getCurrentRequest(), Stapler.getCurrentResponse()); }
  
  protected final void sendError(String message, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
    req.setAttribute("message", message);
    rsp.forward(this, "error", req);
  }
  
  protected final void sendError(String message, StaplerRequest req, StaplerResponse rsp, boolean pre) throws ServletException, IOException {
    req.setAttribute("message", message);
    if (pre)
      req.setAttribute("pre", Boolean.valueOf(true)); 
    rsp.forward(this, "error", req);
  }
  
  protected final void sendError(String message) throws ServletException, IOException { sendError(message, Stapler.getCurrentRequest(), Stapler.getCurrentResponse()); }
  
  @Deprecated
  protected final void requirePOST() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req == null)
      return; 
    String method = req.getMethod();
    if (!method.equalsIgnoreCase("POST"))
      throw new ServletException("Must be POST, Can't be " + method); 
  }
  
  protected SearchIndexBuilder makeSearchIndex() { return (new SearchIndexBuilder()).addAllAnnotations(this); }
  
  public final SearchIndex getSearchIndex() { return makeSearchIndex().make(); }
  
  public Search getSearch() {
    for (SearchFactory sf : SearchFactory.all()) {
      Search s = sf.createFor(this);
      if (s != null)
        return s; 
    } 
    return new Search();
  }
  
  public String getSearchName() { return getDisplayName(); }
}
