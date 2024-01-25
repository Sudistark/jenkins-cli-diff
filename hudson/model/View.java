package hudson.model;

import com.thoughtworks.xstream.io.StreamException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.Indenter;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.DescribableList;
import hudson.util.DescriptorList;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.RunList;
import hudson.util.XStream2;
import hudson.views.ListViewColumn;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.model.item_category.Categories;
import jenkins.model.item_category.Category;
import jenkins.model.item_category.ItemCategory;
import jenkins.util.xml.XMLUtils;
import jenkins.widgets.HasWidgets;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@ExportedBean
public abstract class View extends AbstractModelObject implements AccessControlled, Describable<View>, ExtensionPoint, Saveable, ModelObjectWithChildren, DescriptorByNameOwner, HasWidgets {
  protected ViewGroup owner;
  
  protected String name;
  
  protected String description;
  
  protected boolean filterExecutors;
  
  protected boolean filterQueue;
  
  private static final int FILTER_LOOP_MAX_COUNT = 10;
  
  protected View(String name) {
    this.properties = new PropertyList(this);
    this.name = name;
  }
  
  protected View(String name, ViewGroup owner) {
    this.properties = new PropertyList(this);
    this.name = name;
    this.owner = owner;
  }
  
  public Collection<TopLevelItem> getAllItems() {
    if (this instanceof ViewGroup) {
      Collection<TopLevelItem> items = new LinkedHashSet<TopLevelItem>(getItems());
      for (View view : ((ViewGroup)this).getViews())
        items.addAll(view.getAllItems()); 
      return Collections.unmodifiableCollection(items);
    } 
    return getItems();
  }
  
  public TopLevelItem getItem(String name) { return (TopLevelItem)getOwner().getItemGroup().getItem(name); }
  
  public final TopLevelItem getJob(String name) { return getItem(name); }
  
  @Exported(visibility = 2, name = "name")
  @NonNull
  public String getViewName() { return this.name; }
  
  public void rename(String newName) {
    if (this.name.equals(newName))
      return; 
    Jenkins.checkGoodName(newName);
    if (this.owner.getView(newName) != null)
      throw new Descriptor.FormException(Messages.Hudson_ViewAlreadyExists(newName), "name"); 
    String oldName = this.name;
    this.name = newName;
    this.owner.onViewRenamed(this, oldName, newName);
  }
  
  public ViewGroup getOwner() { return this.owner; }
  
  @Deprecated
  public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() { return this.owner.getItemGroup(); }
  
  @Deprecated
  public View getOwnerPrimaryView() { return this.owner.getPrimaryView(); }
  
  @Deprecated
  public List<Action> getOwnerViewActions() { return this.owner.getViewActions(); }
  
  @Exported
  public String getDescription() { return this.description; }
  
  @DataBoundSetter
  public void setDescription(String description) { this.description = Util.nullify(description); }
  
  public DescribableList<ViewProperty, ViewPropertyDescriptor> getProperties() {
    synchronized (PropertyList.class) {
      if (this.properties == null) {
        this.properties = new PropertyList(this);
      } else {
        this.properties.setOwner(this);
      } 
      return this.properties;
    } 
  }
  
  public List<ViewPropertyDescriptor> getApplicablePropertyDescriptors() {
    List<ViewPropertyDescriptor> r = new ArrayList<ViewPropertyDescriptor>();
    for (ViewPropertyDescriptor pd : ViewProperty.all()) {
      if (pd.isEnabledFor(this))
        r.add(pd); 
    } 
    return r;
  }
  
  public List<ViewPropertyDescriptor> getVisiblePropertyDescriptors() { return DescriptorVisibilityFilter.apply(this, getApplicablePropertyDescriptors()); }
  
  public void save() throws IOException {
    if (this.owner != null)
      this.owner.save(); 
  }
  
  @Exported(name = "property", inline = true)
  public List<ViewProperty> getAllProperties() { return getProperties().toList(); }
  
  public ViewDescriptor getDescriptor() { return (ViewDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public String getDisplayName() { return getViewName(); }
  
  public String getNewPronoun() { return AlternativeUiTextProvider.get(NEW_PRONOUN, this, Messages.AbstractItem_Pronoun()); }
  
  public boolean isEditable() { return true; }
  
  @Deprecated
  public boolean isAutomaticRefreshEnabled() { return false; }
  
  public boolean isFilterExecutors() { return this.filterExecutors; }
  
  @DataBoundSetter
  public void setFilterExecutors(boolean filterExecutors) { this.filterExecutors = filterExecutors; }
  
  public boolean isFilterQueue() { return this.filterQueue; }
  
  @DataBoundSetter
  public void setFilterQueue(boolean filterQueue) { this.filterQueue = filterQueue; }
  
  public Iterable<? extends ListViewColumn> getColumns() { return ListViewColumn.createDefaultInitialColumnList(this); }
  
  public Indenter getIndenter() { return null; }
  
  public boolean isDefault() { return (getOwner().getPrimaryView() == this); }
  
  public List<Computer> getComputers() {
    Computer[] computers = Jenkins.get().getComputers();
    if (!isFilterExecutors())
      return Arrays.asList(computers); 
    List<Computer> result = new ArrayList<Computer>();
    HashSet<Label> labels = new HashSet<Label>();
    for (Item item : getItems()) {
      if (item instanceof AbstractProject)
        labels.addAll(((AbstractProject)item).getRelevantLabels()); 
    } 
    for (Computer c : computers) {
      if (isRelevant(labels, c))
        result.add(c); 
    } 
    return result;
  }
  
  private boolean isRelevant(Collection<Label> labels, Computer computer) {
    Node node = computer.getNode();
    if (node == null)
      return false; 
    if (labels.contains(null) && node.getMode() == Node.Mode.NORMAL)
      return true; 
    for (Label l : labels) {
      if (l != null && l.contains(node))
        return true; 
    } 
    return false;
  }
  
  private List<Queue.Item> filterQueue(List<Queue.Item> base) {
    if (!isFilterQueue())
      return base; 
    Collection<TopLevelItem> items = getItems();
    return (List)base.stream().filter(qi -> filterQueueItemTest(qi, items))
      .collect(Collectors.toList());
  }
  
  private boolean filterQueueItemTest(Queue.Item item, Collection<TopLevelItem> viewItems) {
    Queue.Task currentTask = item.task;
    for (int count = 1;; count++) {
      if (viewItems.contains(currentTask))
        return true; 
      Queue.Task next = currentTask.getOwnerTask();
      if (next == currentTask)
        break; 
      currentTask = next;
      if (count == 10) {
        LOGGER.warning(String.format("Failed to find root task for queue item '%s' for view '%s' in under %d iterations, aborting!", new Object[] { item

                
                .getDisplayName(), getDisplayName(), 
                Integer.valueOf(10) }));
        break;
      } 
    } 
    if (item.task instanceof AbstractProject) {
      AbstractProject<?, ?> project = (AbstractProject)item.task;
      return viewItems.contains(project.getRootProject());
    } 
    return false;
  }
  
  public List<Queue.Item> getQueueItems() { return filterQueue(Arrays.asList(Jenkins.get().getQueue().getItems())); }
  
  @Deprecated
  public List<Queue.Item> getApproximateQueueItemsQuickly() { return filterQueue(Jenkins.get().getQueue().getApproximateItemsQuickly()); }
  
  public String getUrl() { return isDefault() ? ((this.owner != null) ? this.owner.getUrl() : "") : getViewUrl(); }
  
  public String getViewUrl() {
    return ((this.owner != null) ? this.owner.getUrl() : "") + "view/" + ((this.owner != null) ? this.owner.getUrl() : "") + "/";
  }
  
  public String toString() {
    return super.toString() + "[" + super.toString() + "]";
  }
  
  public String getSearchUrl() { return getUrl(); }
  
  public List<Action> getActions() {
    List<Action> result = new ArrayList<Action>();
    result.addAll(getOwner().getViewActions());
    result.addAll(TransientViewActionFactory.createAllFor(this));
    return result;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @Deprecated
  public void updateTransientActions() throws IOException {}
  
  public Object getDynamic(String token) {
    for (Action a : getActions()) {
      String url = a.getUrlName();
      if (url != null && 
        url.equals(token))
        return a; 
    } 
    return null;
  }
  
  @Exported(visibility = 2, name = "url")
  public String getAbsoluteUrl() { return Jenkins.get().getRootUrl() + Jenkins.get().getRootUrl(); }
  
  public Api getApi() { return new Api(this); }
  
  public String getPostConstructLandingPage() { return "configure"; }
  
  @NonNull
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  @Deprecated
  public void onJobRenamed(Item item, String oldName, String newName) {}
  
  @Deprecated
  public boolean hasPeople() { return People.isApplicable(getItems()); }
  
  public People getPeople() { return new People(this); }
  
  public AsynchPeople getAsynchPeople() { return new AsynchPeople(this); }
  
  void addDisplayNamesToSearchIndex(SearchIndexBuilder sib, Collection<TopLevelItem> items) {
    for (TopLevelItem item : items) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine(String.format("Adding url=%s,displayName=%s", new Object[] { item
                .getSearchUrl(), item.getDisplayName() })); 
      sib.add(item.getSearchUrl(), item.getDisplayName());
    } 
  }
  
  protected void makeSearchIndex(SearchIndexBuilder sib) { sib.add(new Object(this)); }
  
  public SearchIndexBuilder makeSearchIndex() {
    SearchIndexBuilder sib = super.makeSearchIndex();
    makeSearchIndex(sib);
    addDisplayNamesToSearchIndex(sib, getItems());
    return sib;
  }
  
  @RequirePOST
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    this.description = req.getParameter("description");
    save();
    rsp.sendRedirect(".");
  }
  
  @POST
  public final void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    submit(req);
    JSONObject json = req.getSubmittedForm();
    setDescription(json.optString("description"));
    setFilterExecutors(json.optBoolean("filterExecutors"));
    setFilterQueue(json.optBoolean("filterQueue"));
    rename(req.getParameter("name"));
    getProperties().rebuild(req, json, getApplicablePropertyDescriptors());
    save();
    FormApply.success("../" + Util.rawEncode(this.name)).generateResponse(req, rsp, this);
  }
  
  @RequirePOST
  public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(DELETE);
    this.owner.deleteView(this);
    rsp.sendRedirect2(req.getContextPath() + "/" + req.getContextPath());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public FormValidation doCheckJobName(@QueryParameter String value) {
    getOwner().checkPermission(Item.CREATE);
    if (Util.fixEmpty(value) == null)
      return FormValidation.ok(); 
    try {
      Jenkins.checkGoodName(value);
      value = value.trim();
      ItemGroup<?> parent = getOwner().getItemGroup();
      Jenkins.get().getProjectNamingStrategy().checkName(parent.getFullName(), value);
    } catch (Failure e) {
      return FormValidation.error(e.getMessage());
    } 
    if (getOwner().getItemGroup().getItem(value) != null)
      return FormValidation.error(Messages.Hudson_JobAlreadyExists(value)); 
    return FormValidation.ok();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Categories doItemCategories(StaplerRequest req, StaplerResponse rsp, @QueryParameter String iconStyle) throws IOException, ServletException {
    String resUrl;
    getOwner().checkPermission(Item.CREATE);
    rsp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    rsp.addHeader("Pragma", "no-cache");
    rsp.addHeader("Expires", "0");
    Categories categories = new Categories();
    int order = 0;
    if (StringUtils.isNotBlank(iconStyle)) {
      resUrl = req.getContextPath() + req.getContextPath();
    } else {
      resUrl = null;
    } 
    for (TopLevelItemDescriptor descriptor : DescriptorVisibilityFilter.apply(getOwner().getItemGroup(), Items.all2(Jenkins.getAuthentication2(), getOwner().getItemGroup()))) {
      ItemCategory ic = ItemCategory.getCategory(descriptor);
      Map<String, Serializable> metadata = new HashMap<String, Serializable>();
      metadata.put("class", descriptor.getId());
      metadata.put("order", Integer.valueOf(++order));
      metadata.put("displayName", descriptor.getDisplayName());
      metadata.put("description", descriptor.getDescription());
      metadata.put("iconFilePathPattern", descriptor.getIconFilePathPattern());
      String iconClassName = descriptor.getIconClassName();
      if (StringUtils.isNotBlank(iconClassName)) {
        metadata.put("iconClassName", iconClassName);
        if (resUrl != null) {
          Icon icon = IconSet.icons.getIconByClassSpec(String.join(" ", new CharSequence[] { iconClassName, iconStyle }));
          if (icon != null)
            metadata.put("iconQualifiedUrl", icon.getQualifiedUrl(resUrl)); 
        } 
      } 
      Category category = categories.getItem(ic.getId());
      if (category != null) {
        category.getItems().add(metadata);
        continue;
      } 
      List<Map<String, Serializable>> temp = new ArrayList<Map<String, Serializable>>();
      temp.add(metadata);
      category = new Category(ic.getId(), ic.getDisplayName(), ic.getDescription(), ic.getOrder(), ic.getMinToShow(), temp);
      categories.getItems().add(category);
    } 
    return categories;
  }
  
  public void doRssAll(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (all builds)", getUrl(), getBuilds().newBuilds()); }
  
  public void doRssFailed(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (failed builds)", getUrl(), getBuilds().failureOnly().newBuilds()); }
  
  public RunList getBuilds() { return new RunList(this); }
  
  public BuildTimelineWidget getTimeline() { return new BuildTimelineWidget(getBuilds()); }
  
  public void doRssLatest(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    List<Run> lastBuilds = new ArrayList<Run>();
    for (TopLevelItem item : getItems()) {
      if (item instanceof Job) {
        Job job = (Job)item;
        Run lb = job.getLastBuild();
        if (lb != null)
          lastBuilds.add(lb); 
      } 
    } 
    RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (latest builds)", getUrl(), RunList.fromRuns(lastBuilds), Run.FEED_ADAPTER_LATEST);
  }
  
  @WebMethod(name = {"config.xml"})
  public HttpResponse doConfigDotXml(StaplerRequest req) throws IOException {
    if (req.getMethod().equals("GET")) {
      checkPermission(READ);
      return new Object(this);
    } 
    if (req.getMethod().equals("POST")) {
      updateByXml(new StreamSource(req.getReader()));
      return HttpResponses.ok();
    } 
    return HttpResponses.error(400, "Unexpected request method " + req.getMethod());
  }
  
  public void writeXml(OutputStream out) throws IOException {
    XStream2 xStream2 = new XStream2();
    xStream2.omitField(View.class, "owner");
    xStream2.toXMLUTF8(this, out);
  }
  
  public void updateByXml(Source source) throws IOException {
    checkPermission(CONFIGURE);
    StringWriter out = new StringWriter();
    try {
      XMLUtils.safeTransform(source, new StreamResult(out));
      out.close();
    } catch (TransformerException|org.xml.sax.SAXException e) {
      throw new IOException("Failed to persist configuration.xml", e);
    } 
    try {
      InputStream in = new BufferedInputStream(new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8)));
      try {
        String oldname = this.name;
        ViewGroup oldOwner = this.owner;
        Object o = Jenkins.XSTREAM2.unmarshal(XStream2.getDefaultDriver().createReader(in), this, null, true);
        if (!o.getClass().equals(getClass()))
          throw new IOException("Expecting view type: " + getClass() + " but got: " + o.getClass() + " instead.\nShould you needed to change to a new view type, you must first delete and then re-create the view with the new view type."); 
        this.name = oldname;
        this.owner = oldOwner;
        in.close();
      } catch (Throwable throwable) {
        try {
          in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (StreamException|com.thoughtworks.xstream.converters.ConversionException|Error e) {
      throw new IOException("Unable to read", e);
    } 
    save();
  }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
    ModelObjectWithContextMenu.ContextMenu m = new ModelObjectWithContextMenu.ContextMenu();
    for (TopLevelItem i : getItems())
      m.add(Functions.getRelativeLinkTo(i), Functions.getRelativeDisplayNameFrom(i, getOwner().getItemGroup())); 
    return m;
  }
  
  @Deprecated
  public static final DescriptorList<View> LIST = new DescriptorList(View.class);
  
  public static DescriptorExtensionList<View, ViewDescriptor> all() { return Jenkins.get().getDescriptorList(View.class); }
  
  @NonNull
  public static List<ViewDescriptor> allInstantiable() {
    r = new ArrayList();
    StaplerRequest request = Stapler.getCurrentRequest();
    if (request == null)
      throw new IllegalStateException("This method can only be invoked from a stapler request"); 
    ViewGroup owner = (ViewGroup)request.findAncestorObject(ViewGroup.class);
    if (owner == null)
      throw new IllegalStateException("This method can only be invoked from a request with a ViewGroup ancestor"); 
    for (ViewDescriptor d : DescriptorVisibilityFilter.apply(owner, all())) {
      if (d.isApplicableIn(owner) && d.isInstantiable() && owner
        .getACL().hasCreatePermission2(Jenkins.getAuthentication2(), owner, d))
        r.add(d); 
    } 
    return r;
  }
  
  public static final Comparator<View> SORTER = Comparator.comparing(View::getViewName);
  
  public static final PermissionGroup PERMISSIONS = new PermissionGroup(View.class, Messages._View_Permissions_Title());
  
  public static final Permission CREATE = new Permission(PERMISSIONS, "Create", Messages._View_CreatePermission_Description(), Permission.CREATE, PermissionScope.ITEM_GROUP);
  
  public static final Permission DELETE = new Permission(PERMISSIONS, "Delete", Messages._View_DeletePermission_Description(), Permission.DELETE, PermissionScope.ITEM_GROUP);
  
  public static final Permission CONFIGURE = new Permission(PERMISSIONS, "Configure", Messages._View_ConfigurePermission_Description(), Permission.CONFIGURE, PermissionScope.ITEM_GROUP);
  
  public static final Permission READ = new Permission(PERMISSIONS, "Read", Messages._View_ReadPermission_Description(), Permission.READ, PermissionScope.ITEM_GROUP);
  
  @Initializer(before = InitMilestone.SYSTEM_CONFIG_LOADED)
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT"}, justification = "to guard against potential future compiler optimizations")
  public static void registerPermissions() throws IOException { Objects.hash(new Object[] { PERMISSIONS, CREATE, DELETE, CONFIGURE, READ }); }
  
  public static Permission getItemCreatePermission() { return Item.CREATE; }
  
  public static View create(StaplerRequest req, StaplerResponse rsp, ViewGroup owner) throws Descriptor.FormException, IOException, ServletException {
    View v;
    String mode = req.getParameter("mode");
    String requestContentType = req.getContentType();
    if (requestContentType == null && (mode == null || 
      !mode.equals("copy")))
      throw new Failure("No Content-Type header set"); 
    boolean isXmlSubmission = (requestContentType != null && (requestContentType.startsWith("application/xml") || requestContentType.startsWith("text/xml")));
    String name = req.getParameter("name");
    Jenkins.checkGoodName(name);
    if (owner.getView(name) != null)
      throw new Failure(Messages.Hudson_ViewAlreadyExists(name)); 
    if (mode == null || mode.isEmpty()) {
      if (isXmlSubmission) {
        v = createViewFromXML(name, req.getInputStream());
        owner.getACL().checkCreatePermission(owner, v.getDescriptor());
        v.owner = owner;
        rsp.setStatus(200);
        return v;
      } 
      throw new Failure(Messages.View_MissingMode());
    } 
    if ("copy".equals(mode)) {
      v = copy(req, owner, name);
    } else {
      ViewDescriptor descriptor = (ViewDescriptor)all().findByName(mode);
      if (descriptor == null)
        throw new Failure("No view type ‘" + mode + "’ is known"); 
      JSONObject submittedForm = req.getSubmittedForm();
      submittedForm.put("name", name);
      v = (View)descriptor.newInstance(req, submittedForm);
    } 
    owner.getACL().checkCreatePermission(owner, v.getDescriptor());
    v.owner = owner;
    rsp.sendRedirect2(req.getContextPath() + "/" + req.getContextPath() + v.getUrl());
    return v;
  }
  
  private static View copy(StaplerRequest req, ViewGroup owner, String name) throws IOException {
    String from = req.getParameter("from");
    View src = owner.getView(from);
    if (src == null) {
      if (Util.fixEmpty(from) == null)
        throw new Failure("Specify which view to copy"); 
      throw new Failure("No such view: " + from);
    } 
    String xml = Jenkins.XSTREAM.toXML(src);
    return createViewFromXML(name, new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset())));
  }
  
  public static View createViewFromXML(String name, InputStream xml) throws IOException {
    try {
      InputStream in = new BufferedInputStream(xml);
      try {
        View v = (View)Jenkins.XSTREAM.fromXML(in);
        if (name != null)
          v.name = name; 
        Jenkins.checkGoodName(v.name);
        View view = v;
        in.close();
        return view;
      } catch (Throwable throwable) {
        try {
          in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (StreamException|com.thoughtworks.xstream.converters.ConversionException|Error e) {
      throw new IOException("Unable to read", e);
    } 
  }
  
  public static final AlternativeUiTextProvider.Message<View> NEW_PRONOUN = new AlternativeUiTextProvider.Message();
  
  private static final Logger LOGGER = Logger.getLogger(View.class.getName());
  
  @Exported(name = "jobs")
  @NonNull
  public abstract Collection<TopLevelItem> getItems();
  
  public abstract boolean contains(TopLevelItem paramTopLevelItem);
  
  protected abstract void submit(StaplerRequest paramStaplerRequest) throws IOException, ServletException, Descriptor.FormException;
  
  public abstract Item doCreateItem(StaplerRequest paramStaplerRequest, StaplerResponse paramStaplerResponse) throws IOException, ServletException;
}
