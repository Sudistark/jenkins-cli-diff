package hudson.model;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.Functions;
import hudson.Util;
import hudson.XmlFile;
import hudson.cli.declarative.CLIResolver;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SaveableListener;
import hudson.model.queue.Executables;
import hudson.model.queue.SubTask;
import hudson.model.queue.Tasks;
import hudson.model.queue.WorkUnit;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessControlled;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.AtomicFileWriter;
import hudson.util.FormValidation;
import hudson.util.IOUtils;
import hudson.util.Secret;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jenkins.model.DirectlyModifiableTopLevelItemGroup;
import jenkins.model.Jenkins;
import jenkins.model.queue.ItemDeletion;
import jenkins.util.SystemProperties;
import jenkins.util.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.HttpDeletable;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.security.access.AccessDeniedException;

@ExportedBean
@BridgeMethodsAdded
public abstract class AbstractItem extends Actionable implements Item, HttpDeletable, AccessControlled, DescriptorByNameOwner, StaplerProxy {
  private static final Logger LOGGER = Logger.getLogger(AbstractItem.class.getName());
  
  protected String name;
  
  private ItemGroup parent;
  
  protected String displayName;
  
  protected AbstractItem(ItemGroup parent, String name) {
    this.parent = parent;
    doSetName(name);
  }
  
  @Exported(visibility = 999)
  public String getName() { return this.name; }
  
  public String getPronoun() { return AlternativeUiTextProvider.get(PRONOUN, this, Messages.AbstractItem_Pronoun()); }
  
  public String getTaskNoun() { return AlternativeUiTextProvider.get(TASK_NOUN, this, Messages.AbstractItem_TaskNoun()); }
  
  @Exported
  public String getDisplayName() {
    if (null != this.displayName)
      return this.displayName; 
    return getName();
  }
  
  @Exported
  public String getDisplayNameOrNull() { return this.displayName; }
  
  public void setDisplayNameOrNull(String displayName) throws IOException { setDisplayName(displayName); }
  
  public void setDisplayName(String displayName) throws IOException {
    this.displayName = Util.fixEmptyAndTrim(displayName);
    save();
  }
  
  public File getRootDir() { return getParent().getRootDirFor(this); }
  
  @WithBridgeMethods(value = {Jenkins.class}, castRequired = true)
  @NonNull
  public ItemGroup getParent() {
    if (this.parent == null)
      throw new IllegalStateException("no parent set on " + getClass().getName() + "[" + this.name + "]"); 
    return this.parent;
  }
  
  @Exported
  public String getDescription() { return this.description; }
  
  public void setDescription(String description) throws IOException {
    this.description = description;
    save();
    ItemListener.fireOnUpdated(this);
  }
  
  protected void doSetName(String name) throws IOException { this.name = name; }
  
  public boolean isNameEditable() { return false; }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse doConfirmRename(@QueryParameter String newName) throws IOException {
    newName = (newName == null) ? null : newName.trim();
    FormValidation validationError = doCheckNewName(newName);
    if (validationError.kind != FormValidation.Kind.OK)
      throw new Failure(validationError.getMessage()); 
    renameTo(newName);
    return HttpResponses.redirectTo("../" + Functions.encode(newName));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public FormValidation doCheckNewName(@QueryParameter String newName) {
    if (!isNameEditable())
      return FormValidation.error("Trying to rename an item that does not support this operation."); 
    if (!hasPermission(Item.CONFIGURE)) {
      if (this.parent instanceof AccessControlled)
        ((AccessControlled)this.parent).checkPermission(Item.CREATE); 
      checkPermission(Item.DELETE);
    } 
    newName = (newName == null) ? null : newName.trim();
    try {
      Jenkins.checkGoodName(newName);
      assert newName != null;
      if (newName.equals(this.name))
        return FormValidation.warning(Messages.AbstractItem_NewNameUnchanged()); 
      Jenkins.get().getProjectNamingStrategy().checkName(getParent().getFullName(), newName);
      checkIfNameIsUsed(newName);
      checkRename(newName);
    } catch (Failure e) {
      return FormValidation.error(e.getMessage());
    } 
    return FormValidation.ok();
  }
  
  private void checkIfNameIsUsed(@NonNull String newName) throws IOException {
    try {
      Item item = getParent().getItem(newName);
      if (item != null)
        throw new Failure(Messages.AbstractItem_NewNameInUse(newName)); 
      ACLContext ctx = ACL.as2(ACL.SYSTEM2);
      try {
        item = getParent().getItem(newName);
        if (item != null) {
          if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, "Unable to rename the job {0}: name {1} is already in use. User {2} has no {3} permission for existing job with the same name", new Object[] { getFullName(), newName, ctx.getPreviousContext2().getAuthentication().getName(), Item.DISCOVER.name }); 
          throw new Failure(Messages.Jenkins_NotAllowedName(newName));
        } 
        if (ctx != null)
          ctx.close(); 
      } catch (Throwable throwable) {
        if (ctx != null)
          try {
            ctx.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (AccessDeniedException ex) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.log(Level.FINE, "Unable to rename the job {0}: name {1} is already in use. User {2} has {3} permission, but no {4} for existing job with the same name", new Object[] { getFullName(), newName, User.current(), Item.DISCOVER.name, Item.READ.name }); 
      throw new Failure(Messages.AbstractItem_NewNameInUse(newName));
    } 
  }
  
  protected void checkRename(@NonNull String newName) throws IOException {}
  
  @SuppressFBWarnings(value = {"SWL_SLEEP_WITH_LOCK_HELD"}, justification = "no big deal")
  protected void renameTo(String newName) throws IOException {
    if (!isNameEditable())
      throw new IOException("Trying to rename an item that does not support this operation."); 
    ItemGroup parent = getParent();
    oldName = this.name;
    String oldFullName = getFullName();
    synchronized (parent) {
      synchronized (this) {
        if (newName == null)
          throw new IllegalArgumentException("New name is not given"); 
        if (this.name.equals(newName))
          return; 
        Items.verifyItemDoesNotAlreadyExist(parent, newName, this);
        File oldRoot = getRootDir();
        doSetName(newName);
        File newRoot = getRootDir();
        success = false;
        try {
          boolean interrupted = false;
          boolean renamed = false;
          for (int retry = 0; retry < 5; retry++) {
            if (oldRoot.renameTo(newRoot)) {
              renamed = true;
              break;
            } 
            try {
              Thread.sleep(500L);
            } catch (InterruptedException e) {
              interrupted = true;
            } 
          } 
          if (interrupted)
            Thread.currentThread().interrupt(); 
          if (!renamed) {
            Copy cp = new Copy();
            cp.setProject(new Project());
            cp.setTodir(newRoot);
            FileSet src = new FileSet();
            src.setDir(oldRoot);
            cp.addFileset(src);
            cp.setOverwrite(true);
            cp.setPreserveLastModified(true);
            cp.setFailOnError(false);
            cp.execute();
            try {
              Util.deleteRecursive(oldRoot);
            } catch (IOException e) {
              e.printStackTrace();
            } 
          } 
          success = true;
        } finally {
          if (!success)
            doSetName(oldName); 
        } 
        parent.onRenamed(this, oldName, newName);
      } 
    } 
    ItemListener.fireLocationChange(this, oldFullName);
  }
  
  public void movedTo(DirectlyModifiableTopLevelItemGroup destination, AbstractItem newItem, File destDir) throws IOException { newItem.onLoad(destination, this.name); }
  
  @Exported
  public final String getFullName() {
    String n = getParent().getFullName();
    if (n.isEmpty())
      return getName(); 
    return n + "/" + n;
  }
  
  @Exported
  public final String getFullDisplayName() {
    String n = getParent().getFullDisplayName();
    if (n.isEmpty())
      return getDisplayName(); 
    return n + " » " + n;
  }
  
  public String getRelativeDisplayNameFrom(ItemGroup p) { return Functions.getRelativeDisplayNameFrom(this, p); }
  
  public String getRelativeNameFromGroup(ItemGroup p) { return getRelativeNameFrom(p); }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    this.parent = parent;
    doSetName(name);
  }
  
  public void onCopiedFrom(Item src) {}
  
  public final String getUrl() {
    StaplerRequest req = Stapler.getCurrentRequest();
    String shortUrl = getShortUrl();
    String uri = (req == null) ? null : req.getRequestURI();
    if (req != null) {
      String seed = Functions.getNearestAncestorUrl(req, this);
      LOGGER.log(Level.FINER, "seed={0} for {1} from {2}", new Object[] { seed, this, uri });
      if (seed != null)
        return seed.substring(req.getContextPath().length() + 1) + "/"; 
      List<Ancestor> ancestors = req.getAncestors();
      if (!ancestors.isEmpty()) {
        Ancestor last = (Ancestor)ancestors.get(ancestors.size() - 1);
        if (last.getObject() instanceof View) {
          View view = (View)last.getObject();
          if (view.getOwner().getItemGroup() == getParent() && !view.isDefault()) {
            String prefix = req.getContextPath() + "/";
            String url = last.getUrl();
            if (url.startsWith(prefix)) {
              String base = url.substring(prefix.length()) + "/";
              LOGGER.log(Level.FINER, "using {0}{1} for {2} from {3} given {4}", new Object[] { base, shortUrl, this, uri, prefix });
              return base + base;
            } 
            LOGGER.finer(() -> url + " does not start with " + url + " as expected");
          } else {
            LOGGER.log(Level.FINER, "irrelevant {0} for {1} from {2}", new Object[] { view.getViewName(), this, uri });
          } 
        } else {
          LOGGER.log(Level.FINER, "inapplicable {0} for {1} from {2}", new Object[] { last.getObject(), this, uri });
        } 
      } else {
        LOGGER.log(Level.FINER, "no ancestors for {0} from {1}", new Object[] { this, uri });
      } 
    } else {
      LOGGER.log(Level.FINER, "no current request for {0}", this);
    } 
    String base = getParent().getUrl();
    LOGGER.log(Level.FINER, "falling back to {0}{1} for {2} from {3}", new Object[] { base, shortUrl, this, uri });
    return base + base;
  }
  
  public String getShortUrl() {
    String prefix = getParent().getUrlChildPrefix();
    String subdir = Util.rawEncode(getName());
    return prefix.equals(".") ? (subdir + "/") : (prefix + "/" + prefix + "/");
  }
  
  public String getSearchUrl() { return getShortUrl(); }
  
  @Exported(visibility = 999, name = "url")
  public final String getAbsoluteUrl() { return super.getAbsoluteUrl(); }
  
  public final Api getApi() { return new Api(this); }
  
  @NonNull
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    getConfigFile().write(this);
    SaveableListener.fireOnChange(this, getConfigFile());
  }
  
  public final XmlFile getConfigFile() { return Items.getConfigFile(this); }
  
  protected Object writeReplace() { return XmlFile.replaceIfNotAtTopLevel(this, () -> new Replacer(this)); }
  
  @RequirePOST
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    setDescription(req.getParameter("description"));
    rsp.sendRedirect(".");
  }
  
  @RequirePOST
  public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    delete();
    if (req == null || rsp == null)
      return; 
    List<Ancestor> ancestors = req.getAncestors();
    ListIterator<Ancestor> it = ancestors.listIterator(ancestors.size());
    String url = getParent().getUrl();
    while (it.hasPrevious()) {
      Object a = ((Ancestor)it.previous()).getObject();
      if (a instanceof View) {
        url = ((View)a).getUrl();
        break;
      } 
      if (a instanceof ViewGroup && a != this) {
        url = ((ViewGroup)a).getUrl();
        break;
      } 
    } 
    rsp.sendRedirect2(req.getContextPath() + "/" + req.getContextPath());
  }
  
  public void delete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    try {
      delete();
      rsp.setStatus(204);
    } catch (InterruptedException e) {
      throw new ServletException(e);
    } 
  }
  
  public void delete() throws IOException {
    checkPermission(DELETE);
    boolean responsibleForAbortingBuilds = !ItemDeletion.contains(this);
    ownsRegistration = ItemDeletion.register(this);
    if (!ownsRegistration && ItemDeletion.isRegistered(this))
      throw new Failure(Messages.AbstractItem_BeingDeleted(getPronoun())); 
    try {
      if (responsibleForAbortingBuilds || ownsRegistration) {
        Queue queue = Queue.getInstance();
        if (this instanceof Queue.Task)
          queue.cancel((Queue.Task)this); 
        for (Queue.Item i : queue.getItems()) {
          Item item = Tasks.getItemOf(i.task);
          while (item != null) {
            if (item == this) {
              queue.cancel(i);
              break;
            } 
            if (item.getParent() instanceof Item)
              item = (Item)item.getParent(); 
          } 
        } 
        Map<Executor, Queue.Executable> buildsInProgress = new LinkedHashMap<Executor, Queue.Executable>();
        for (Computer c : Jenkins.get().getComputers()) {
          for (Executor e : c.getAllExecutors()) {
            WorkUnit workUnit = e.getCurrentWorkUnit();
            Queue.Executable executable = (workUnit != null) ? workUnit.getExecutable() : null;
            SubTask subtask = (executable != null) ? Executables.getParentOf(executable) : null;
            if (subtask != null) {
              Item item = Tasks.getItemOf(subtask);
              while (item != null) {
                if (item == this) {
                  buildsInProgress.put(e, e.getCurrentExecutable());
                  e.interrupt(Result.ABORTED);
                  break;
                } 
                if (item.getParent() instanceof Item)
                  item = (Item)item.getParent(); 
              } 
            } 
          } 
        } 
        if (!buildsInProgress.isEmpty()) {
          long expiration = System.nanoTime() + TimeUnit.SECONDS.toNanos(15L);
          while (!buildsInProgress.isEmpty() && expiration - System.nanoTime() > 0L) {
            Iterator<Map.Entry<Executor, Queue.Executable>> iterator = buildsInProgress.entrySet().iterator();
            while (iterator.hasNext()) {
              Map.Entry<Executor, Queue.Executable> entry = (Map.Entry)iterator.next();
              if (!((Executor)entry.getKey()).isAlive() || entry
                .getValue() != ((Executor)entry.getKey()).getCurrentExecutable())
                iterator.remove(); 
              ((Executor)entry.getKey()).interrupt(Result.ABORTED);
            } 
            Thread.sleep(50L);
          } 
          if (!buildsInProgress.isEmpty())
            throw new Failure(Messages.AbstractItem_FailureToStopBuilds(
                  Integer.valueOf(buildsInProgress.size()), getFullDisplayName())); 
        } 
      } 
      synchronized (this) {
        performDelete();
      } 
    } finally {
      if (ownsRegistration)
        ItemDeletion.deregister(this); 
    } 
    getParent().onDeleted(this);
    Jenkins.get().rebuildDependencyGraphAsync();
  }
  
  protected void performDelete() throws IOException {
    getConfigFile().delete();
    Util.deleteRecursive(getRootDir());
  }
  
  @WebMethod(name = {"config.xml"})
  public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (req.getMethod().equals("GET")) {
      rsp.setContentType("application/xml");
      writeConfigDotXml(rsp.getOutputStream());
      return;
    } 
    if (req.getMethod().equals("POST")) {
      updateByXml(new StreamSource(req.getReader()));
      return;
    } 
    rsp.sendError(400);
  }
  
  static final Pattern SECRET_PATTERN = Pattern.compile(">(" + Secret.ENCRYPTED_VALUE_PATTERN + ")<");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void writeConfigDotXml(OutputStream os) throws IOException {
    checkPermission(EXTENDED_READ);
    XmlFile configFile = getConfigFile();
    if (hasPermission(CONFIGURE)) {
      IOUtils.copy(configFile.getFile(), os);
    } else {
      String encoding = configFile.sniffEncoding();
      String xml = Files.readString(Util.fileToPath(configFile.getFile()), Charset.forName(encoding));
      Matcher matcher = SECRET_PATTERN.matcher(xml);
      StringBuilder cleanXml = new StringBuilder();
      while (matcher.find()) {
        if (Secret.decrypt(matcher.group(true)) != null)
          matcher.appendReplacement(cleanXml, ">********<"); 
      } 
      matcher.appendTail(cleanXml);
      IOUtils.write(cleanXml.toString(), os, encoding);
    } 
  }
  
  @Deprecated
  public void updateByXml(StreamSource source) throws IOException { updateByXml(source); }
  
  public void updateByXml(Source source) throws IOException {
    checkPermission(CONFIGURE);
    XmlFile configXmlFile = getConfigFile();
    out = new AtomicFileWriter(configXmlFile.getFile());
    try {
      try {
        XMLUtils.safeTransform(source, new StreamResult(out));
        out.close();
      } catch (TransformerException|org.xml.sax.SAXException e) {
        throw new IOException("Failed to persist config.xml", e);
      } 
      Object o = (new XmlFile(Items.XSTREAM, out.getTemporaryPath().toFile())).unmarshalNullingOut(this);
      if (o != this)
        throw new IOException("Expecting " + getClass() + " but got " + o.getClass() + " instead"); 
      Items.whileUpdatingByXml(new Object(this));
      Jenkins.get().rebuildDependencyGraphAsync();
      out.commit();
      SaveableListener.fireOnChange(this, getConfigFile());
    } finally {
      out.abort();
    } 
  }
  
  @RequirePOST
  public void doReload() throws IOException {
    checkPermission(CONFIGURE);
    getConfigFile().unmarshal(this);
    Items.whileUpdatingByXml(new Object(this));
    Jenkins.get().rebuildDependencyGraphAsync();
  }
  
  public String getSearchName() { return getName(); }
  
  public String toString() {
    return super.toString() + "[" + super.toString() + "]";
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK) {
      if (!hasPermission(Item.DISCOVER))
        return null; 
      checkPermission(Item.READ);
    } 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(AbstractItem.class.getName() + ".skipPermissionCheck");
  
  @CLIResolver
  public static AbstractItem resolveForCLI(@Argument(required = true, metaVar = "NAME", usage = "Item name") String name) throws CmdLineException {
    AbstractItem item = (AbstractItem)Jenkins.get().getItemByFullName(name, AbstractItem.class);
    if (item == null) {
      AbstractItem project = (AbstractItem)Items.findNearest(AbstractItem.class, name, Jenkins.get());
      throw new CmdLineException(null, (project == null) ? Messages.AbstractItem_NoSuchJobExistsWithoutSuggestion(name) : 
          Messages.AbstractItem_NoSuchJobExists(name, project.getFullName()));
    } 
    return item;
  }
  
  public static final AlternativeUiTextProvider.Message<AbstractItem> PRONOUN = new AlternativeUiTextProvider.Message();
  
  public static final AlternativeUiTextProvider.Message<AbstractItem> TASK_NOUN = new AlternativeUiTextProvider.Message();
  
  public abstract Collection<? extends Job> getAllJobs();
}
