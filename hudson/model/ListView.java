package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.diagnosis.OldDataMonitor;
import hudson.search.SearchIndexBuilder;
import hudson.util.DescribableList;
import hudson.util.HttpResponses;
import hudson.views.ListViewColumn;
import hudson.views.StatusFilter;
import hudson.views.ViewJobFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.jcip.annotations.GuardedBy;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.access.AccessDeniedException;

public class ListView extends View implements DirectlyModifiableView {
  @GuardedBy("this")
  SortedSet<String> jobNames = new TreeSet(String.CASE_INSENSITIVE_ORDER);
  
  private DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> jobFilters;
  
  private DescribableList<ListViewColumn, Descriptor<ListViewColumn>> columns;
  
  private String includeRegex;
  
  private boolean recurse;
  
  private Pattern includePattern;
  
  @Deprecated
  private Boolean statusFilter;
  
  @DataBoundConstructor
  public ListView(String name) {
    super(name);
    initColumns();
    initJobFilters();
  }
  
  public ListView(String name, ViewGroup owner) {
    this(name);
    this.owner = owner;
  }
  
  @DataBoundSetter
  public void setColumns(List<ListViewColumn> columns) throws IOException { this.columns.replaceBy(columns); }
  
  @DataBoundSetter
  public void setJobFilters(List<ViewJobFilter> jobFilters) throws IOException { this.jobFilters.replaceBy(jobFilters); }
  
  protected Object readResolve() {
    if (this.includeRegex != null)
      try {
        this.includePattern = Pattern.compile(this.includeRegex);
      } catch (PatternSyntaxException x) {
        this.includeRegex = null;
        OldDataMonitor.report(this, Set.of(x));
      }  
    synchronized (this) {
      if (this.jobNames == null)
        this.jobNames = new TreeSet(String.CASE_INSENSITIVE_ORDER); 
    } 
    initColumns();
    initJobFilters();
    if (this.statusFilter != null)
      this.jobFilters.add(new StatusFilter(this.statusFilter.booleanValue())); 
    return this;
  }
  
  protected void initColumns() {
    if (this.columns == null)
      this
        .columns = new DescribableList(this, ListViewColumn.createDefaultInitialColumnList(getClass())); 
  }
  
  protected void initJobFilters() {
    if (this.jobFilters == null)
      this.jobFilters = new DescribableList(this); 
  }
  
  public boolean hasJobFilterExtensions() { return !ViewJobFilter.all().isEmpty(); }
  
  public DescribableList<ViewJobFilter, Descriptor<ViewJobFilter>> getJobFilters() { return this.jobFilters; }
  
  public DescribableList<ListViewColumn, Descriptor<ListViewColumn>> getColumns() { return this.columns; }
  
  public Set<String> getJobNames() { return Collections.unmodifiableSet(this.jobNames); }
  
  public List<TopLevelItem> getItems() { return getItems(this.recurse); }
  
  private List<TopLevelItem> getItems(boolean recurse) {
    SortedSet<String> names;
    items = new ArrayList<TopLevelItem>();
    synchronized (this) {
      names = new TreeSet<String>(this.jobNames);
    } 
    ItemGroup<? extends TopLevelItem> parent = getOwner().getItemGroup();
    if (recurse) {
      if (!names.isEmpty() || this.includePattern != null)
        items.addAll(parent.getAllItems(TopLevelItem.class, item -> {
                String itemName = item.getRelativeNameFrom(parent);
                if (names.contains(itemName))
                  return true; 
                if (this.includePattern != null)
                  return this.includePattern.matcher(itemName).matches(); 
                return false;
              })); 
    } else {
      for (String name : names) {
        try {
          TopLevelItem i = (TopLevelItem)parent.getItem(name);
          if (i != null)
            items.add(i); 
        } catch (AccessDeniedException accessDeniedException) {}
      } 
      if (this.includePattern != null)
        items.addAll(parent.getItems(item -> {
                String itemName = item.getRelativeNameFrom(parent);
                return this.includePattern.matcher(itemName).matches();
              })); 
    } 
    DescribableList describableList = getJobFilters();
    if (!describableList.isEmpty()) {
      List<TopLevelItem> candidates = recurse ? parent.getAllItems(TopLevelItem.class) : new ArrayList(parent.getItems());
      for (ViewJobFilter jobFilter : describableList)
        items = jobFilter.filter(items, candidates, this); 
    } 
    return new ArrayList<TopLevelItem>(new LinkedHashSet(items));
  }
  
  public SearchIndexBuilder makeSearchIndex() {
    SearchIndexBuilder sib = (new SearchIndexBuilder()).addAllAnnotations(this);
    makeSearchIndex(sib);
    addDisplayNamesToSearchIndex(sib, getItems(true));
    return sib;
  }
  
  public boolean contains(TopLevelItem item) { return getItems().contains(item); }
  
  public boolean jobNamesContains(TopLevelItem item) {
    if (item == null)
      return false; 
    return this.jobNames.contains(item.getRelativeNameFrom(getOwner().getItemGroup()));
  }
  
  public void add(TopLevelItem item) throws IOException {
    synchronized (this) {
      this.jobNames.add(item.getRelativeNameFrom(getOwner().getItemGroup()));
    } 
    save();
  }
  
  public boolean remove(TopLevelItem item) {
    synchronized (this) {
      String name = item.getRelativeNameFrom(getOwner().getItemGroup());
      if (!this.jobNames.remove(name))
        return false; 
    } 
    save();
    return true;
  }
  
  public String getIncludeRegex() { return this.includeRegex; }
  
  public boolean isRecurse() { return this.recurse; }
  
  @DataBoundSetter
  public void setRecurse(boolean recurse) { this.recurse = recurse; }
  
  @Deprecated
  public Boolean getStatusFilter() { return this.statusFilter; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isAddToCurrentView() {
    synchronized (this) {
      return (!this.jobNames.isEmpty() || (this.jobFilters
        .isEmpty() && this.includePattern == null));
    } 
  }
  
  private boolean needToAddToCurrentView(StaplerRequest req) throws ServletException {
    String json = req.getParameter("json");
    if (json != null && json.length() > 0) {
      JSONObject form = req.getSubmittedForm();
      return (form.has("addToCurrentView") && form.getBoolean("addToCurrentView"));
    } 
    return true;
  }
  
  @POST
  public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    ItemGroup<? extends TopLevelItem> ig = getOwner().getItemGroup();
    if (ig instanceof ModifiableItemGroup) {
      TopLevelItem item = (TopLevelItem)((ModifiableItemGroup)ig).doCreateItem(req, rsp);
      if (item != null && 
        needToAddToCurrentView(req)) {
        synchronized (this) {
          this.jobNames.add(item.getRelativeNameFrom(getOwner().getItemGroup()));
        } 
        this.owner.save();
      } 
      return item;
    } 
    return null;
  }
  
  @RequirePOST
  public HttpResponse doAddJobToView(@QueryParameter String name) throws IOException, ServletException {
    checkPermission(View.CONFIGURE);
    if (name == null)
      throw new Failure("Query parameter 'name' is required"); 
    TopLevelItem item = resolveName(name);
    if (item == null)
      throw new Failure("Query parameter 'name' does not correspond to a known item"); 
    if (contains(item))
      return HttpResponses.ok(); 
    add(item);
    this.owner.save();
    return HttpResponses.ok();
  }
  
  @RequirePOST
  public HttpResponse doRemoveJobFromView(@QueryParameter String name) throws IOException, ServletException {
    checkPermission(View.CONFIGURE);
    if (name == null)
      throw new Failure("Query parameter 'name' is required"); 
    TopLevelItem item = resolveName(name);
    if (item == null)
      throw new Failure("Query parameter 'name' does not correspond to a known and readable item"); 
    if (remove(item))
      this.owner.save(); 
    return HttpResponses.ok();
  }
  
  @CheckForNull
  private TopLevelItem resolveName(String name) {
    TopLevelItem item = (TopLevelItem)getOwner().getItemGroup().getItem(name);
    if (item == null) {
      name = Items.getCanonicalName(getOwner().getItemGroup(), name);
      item = (TopLevelItem)Jenkins.get().getItemByFullName(name, TopLevelItem.class);
    } 
    return item;
  }
  
  protected void submit(StaplerRequest req) throws ServletException, Descriptor.FormException, IOException {
    JSONObject json = req.getSubmittedForm();
    synchronized (this) {
      Iterable<? extends TopLevelItem> items;
      this.recurse = json.optBoolean("recurse", true);
      this.jobNames.clear();
      if (this.recurse) {
        items = getOwner().getItemGroup().getAllItems(TopLevelItem.class);
      } else {
        items = getOwner().getItemGroup().getItems();
      } 
      for (TopLevelItem item : items) {
        String relativeNameFrom = item.getRelativeNameFrom(getOwner().getItemGroup());
        if (req.getParameter("item_" + relativeNameFrom) != null)
          this.jobNames.add(relativeNameFrom); 
      } 
    } 
    setIncludeRegex((req.getParameter("useincluderegex") != null) ? req.getParameter("includeRegex") : null);
    if (this.columns == null)
      this.columns = new DescribableList(this); 
    this.columns.rebuildHetero(req, json, ListViewColumn.all(), "columns");
    if (this.jobFilters == null)
      this.jobFilters = new DescribableList(this); 
    this.jobFilters.rebuildHetero(req, json, ViewJobFilter.all(), "jobFilters");
    String filter = Util.fixEmpty(req.getParameter("statusFilter"));
    this.statusFilter = (filter != null) ? Boolean.valueOf("1".equals(filter)) : null;
  }
  
  @DataBoundSetter
  public void setIncludeRegex(String includeRegex) {
    this.includeRegex = Util.nullify(includeRegex);
    if (this.includeRegex == null) {
      this.includePattern = null;
    } else {
      this.includePattern = Pattern.compile(includeRegex);
    } 
  }
  
  @DataBoundSetter
  public void setJobNames(Set<String> jobNames) { this.jobNames = new TreeSet(jobNames); }
  
  @Deprecated
  @DataBoundSetter
  public void setStatusFilter(Boolean statusFilter) { this.statusFilter = statusFilter; }
  
  @Deprecated
  public static List<ListViewColumn> getDefaultColumns() { return ListViewColumn.createDefaultInitialColumnList(ListView.class); }
}
