package hudson.widgets;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Functions;
import hudson.model.ModelObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import jenkins.util.SystemProperties;
import jenkins.widgets.HistoryPageEntry;
import jenkins.widgets.HistoryPageFilter;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class HistoryWidget<O extends ModelObject, T> extends Widget {
  public Iterable<T> baseList;
  
  private String nextBuildNumberToFetch;
  
  public final String baseUrl;
  
  public final O owner;
  
  private boolean trimmed;
  
  public final Adapter<? super T> adapter;
  
  final Long newerThan;
  
  final Long olderThan;
  
  final String searchString;
  
  private String firstTransientBuildKey;
  
  public HistoryWidget(O owner, Iterable<T> baseList, Adapter<? super T> adapter) {
    StaplerRequest currentRequest = Stapler.getCurrentRequest();
    this.adapter = adapter;
    this.baseList = baseList;
    this.baseUrl = Functions.getNearestAncestorUrl(currentRequest, owner);
    this.owner = owner;
    this.newerThan = getPagingParam(currentRequest, "newer-than");
    this.olderThan = getPagingParam(currentRequest, "older-than");
    this.searchString = currentRequest.getParameter("search");
  }
  
  protected String getOwnerUrl() { return this.baseUrl; }
  
  public String getDisplayName() { return Messages.BuildHistoryWidget_DisplayName(); }
  
  public String getUrlName() { return "buildHistory"; }
  
  public String getFirstTransientBuildKey() { return this.firstTransientBuildKey; }
  
  protected HistoryPageFilter updateFirstTransientBuildKey(HistoryPageFilter historyPageFilter) {
    updateFirstTransientBuildKey(historyPageFilter.runs);
    return historyPageFilter;
  }
  
  private Iterable<HistoryPageEntry<T>> updateFirstTransientBuildKey(Iterable<HistoryPageEntry<T>> source) {
    String key = null;
    for (HistoryPageEntry<T> t : source) {
      if (this.adapter.isBuilding(t.getEntry()))
        key = this.adapter.getKey(t.getEntry()); 
    } 
    this.firstTransientBuildKey = key;
    return source;
  }
  
  public Iterable<HistoryPageEntry<T>> getRenderList() {
    if (this.trimmed) {
      List<HistoryPageEntry<T>> pageEntries = toPageEntries(this.baseList);
      if (pageEntries.size() > THRESHOLD)
        return updateFirstTransientBuildKey(pageEntries.subList(0, THRESHOLD)); 
      this.trimmed = false;
      return updateFirstTransientBuildKey(pageEntries);
    } 
    return updateFirstTransientBuildKey(toPageEntries(this.baseList));
  }
  
  private List<HistoryPageEntry<T>> toPageEntries(Iterable<T> historyItemList) {
    Iterator<T> iterator = historyItemList.iterator();
    if (!iterator.hasNext())
      return Collections.emptyList(); 
    List<HistoryPageEntry<T>> pageEntries = new ArrayList<HistoryPageEntry<T>>();
    while (iterator.hasNext())
      pageEntries.add(new HistoryPageEntry(iterator.next())); 
    return pageEntries;
  }
  
  public HistoryPageFilter getHistoryPageFilter() {
    HistoryPageFilter<T> historyPageFilter = newPageFilter();
    historyPageFilter.add(this.baseList);
    historyPageFilter.widget = this;
    return updateFirstTransientBuildKey(historyPageFilter);
  }
  
  protected HistoryPageFilter<T> newPageFilter() {
    HistoryPageFilter<T> historyPageFilter = new HistoryPageFilter<T>(THRESHOLD);
    if (this.newerThan != null) {
      historyPageFilter.setNewerThan(this.newerThan);
    } else if (this.olderThan != null) {
      historyPageFilter.setOlderThan(this.olderThan);
    } 
    if (this.searchString != null)
      historyPageFilter.setSearchString(this.searchString); 
    return historyPageFilter;
  }
  
  public boolean isTrimmed() { return this.trimmed; }
  
  public void setTrimmed(boolean trimmed) { this.trimmed = trimmed; }
  
  public void doAjax(StaplerRequest req, StaplerResponse rsp, @Header("n") String n) throws IOException, ServletException {
    rsp.setContentType("text/html;charset=UTF-8");
    List<T> items = new ArrayList<T>();
    if (n != null) {
      String nn = null;
      for (T t : this.baseList) {
        if (this.adapter.compare(t, n) >= 0) {
          items.add(t);
          if (this.adapter.isBuilding(t))
            nn = this.adapter.getKey(t); 
        } 
      } 
      if (nn == null)
        if (items.isEmpty()) {
          nn = n;
        } else {
          nn = this.adapter.getNextKey(this.adapter.getKey(items.get(0)));
        }  
      this.baseList = items;
      rsp.setHeader("n", nn);
      this.firstTransientBuildKey = nn;
    } 
    HistoryPageFilter page = getHistoryPageFilter();
    req.getView(page, "ajaxBuildHistory.jelly").forward(req, rsp);
  }
  
  static final int THRESHOLD = SystemProperties.getInteger(HistoryWidget.class.getName() + ".threshold", Integer.valueOf(30)).intValue();
  
  public String getNextBuildNumberToFetch() { return this.nextBuildNumberToFetch; }
  
  public void setNextBuildNumberToFetch(String nextBuildNumberToFetch) { this.nextBuildNumberToFetch = nextBuildNumberToFetch; }
  
  private Long getPagingParam(@CheckForNull StaplerRequest currentRequest, @CheckForNull String name) {
    if (currentRequest == null || name == null)
      return null; 
    String paramVal = currentRequest.getParameter(name);
    if (paramVal == null)
      return null; 
    try {
      return Long.valueOf(paramVal);
    } catch (NumberFormatException nfe) {
      return null;
    } 
  }
}
