package hudson.model;

import hudson.FeedAdapter;
import hudson.util.RunList;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public final class RSS {
  public static <E> void forwardToRss(String title, String url, Collection<? extends E> entries, FeedAdapter<E> adapter, StaplerRequest req, HttpServletResponse rsp) throws IOException, ServletException {
    req.setAttribute("adapter", adapter);
    req.setAttribute("title", title);
    req.setAttribute("url", url);
    req.setAttribute("entries", entries);
    String flavor = req.getParameter("flavor");
    if (flavor == null)
      flavor = "atom"; 
    flavor = flavor.replace('/', '_');
    if (flavor.equals("atom")) {
      rsp.setContentType("application/atom+xml; charset=UTF-8");
    } else {
      rsp.setContentType("text/xml; charset=UTF-8");
    } 
    req.getView(Jenkins.get(), "/hudson/" + flavor + ".jelly").forward(req, rsp);
  }
  
  public static void rss(StaplerRequest req, StaplerResponse rsp, String title, String url, RunList runList) throws IOException, ServletException { rss(req, rsp, title, url, runList, null); }
  
  public static void rss(StaplerRequest req, StaplerResponse rsp, String title, String url, RunList runList, FeedAdapter<Run> feedAdapter) throws IOException, ServletException {
    FeedAdapter<Run> feedAdapter_ = (feedAdapter == null) ? Run.FEED_ADAPTER : feedAdapter;
    forwardToRss(title, url, runList, feedAdapter_, req, rsp);
  }
}
