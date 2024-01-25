package hudson.model;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.ExtensionList;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.listeners.SaveableListener;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.SecurityRealm;
import hudson.security.UserMayOrMayNotExistException2;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.RunList;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.IdStrategy;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.scm.RunWithSCM;
import jenkins.security.ImpersonatingUserDetailsService2;
import jenkins.security.UserDetailsCache;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExportedBean
@BridgeMethodsAdded
public class User extends AbstractModelObject implements AccessControlled, DescriptorByNameOwner, Saveable, Comparable<User>, ModelObjectWithContextMenu, StaplerProxy {
  public static final XStream2 XSTREAM = new XStream2();
  
  private static final Logger LOGGER = Logger.getLogger(User.class.getName());
  
  static final String CONFIG_XML = "config.xml";
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(User.class.getName() + ".skipPermissionCheck");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean ALLOW_NON_EXISTENT_USER_TO_LOGIN = SystemProperties.getBoolean(User.class.getName() + ".allowNonExistentUserToLogin");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean ALLOW_USER_CREATION_VIA_URL = SystemProperties.getBoolean(User.class.getName() + ".allowUserCreationViaUrl");
  
  private static final String UNKNOWN_USERNAME = "unknown";
  
  private static final String[] ILLEGAL_PERSISTED_USERNAMES = { "anonymous", "SYSTEM", "unknown" };
  
  private final int version = 10;
  
  private String id;
  
  static  {
    XSTREAM.alias("user", User.class);
  }
  
  private User(String id, String fullName) {
    this.version = 10;
    this.properties = new ArrayList();
    this.id = id;
    this.fullName = fullName;
    load(id);
  }
  
  private void load(String userId) {
    clearExistingProperties();
    loadFromUserConfigFile(userId);
    removeNullsThatFailedToLoad();
    allocateDefaultPropertyInstancesAsNeeded();
    setUserToProperties();
  }
  
  private void setUserToProperties() {
    for (UserProperty p : this.properties)
      p.setUser(this); 
  }
  
  private void allocateDefaultPropertyInstancesAsNeeded() {
    for (UserPropertyDescriptor d : UserProperty.all()) {
      if (getProperty(d.clazz) == null) {
        UserProperty up = d.newInstance(this);
        if (up != null)
          this.properties.add(up); 
      } 
    } 
  }
  
  private void removeNullsThatFailedToLoad() { this.properties.removeIf(Objects::isNull); }
  
  private void loadFromUserConfigFile(String userId) {
    XmlFile config = getConfigFile();
    try {
      if (config != null && config.exists()) {
        config.unmarshal(this);
        this.id = userId;
      } 
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to load " + config, e);
    } 
  }
  
  private void clearExistingProperties() { this.properties.clear(); }
  
  private XmlFile getConfigFile() {
    File existingUserFolder = getExistingUserFolder();
    return (existingUserFolder == null) ? null : new XmlFile(XSTREAM, new File(existingUserFolder, "config.xml"));
  }
  
  @NonNull
  public static IdStrategy idStrategy() {
    j = Jenkins.get();
    SecurityRealm realm = j.getSecurityRealm();
    if (realm == null)
      return IdStrategy.CASE_INSENSITIVE; 
    return realm.getUserIdStrategy();
  }
  
  public int compareTo(@NonNull User that) { return idStrategy().compare(this.id, that.id); }
  
  @Exported
  public String getId() { return this.id; }
  
  @NonNull
  public String getUrl() { return "user/" + Util.rawEncode(idStrategy().keyFor(this.id)); }
  
  @NonNull
  public String getSearchUrl() { return "/user/" + Util.rawEncode(idStrategy().keyFor(this.id)); }
  
  @Exported(visibility = 999)
  @NonNull
  public String getAbsoluteUrl() { return Jenkins.get().getRootUrl() + Jenkins.get().getRootUrl(); }
  
  @Exported(visibility = 999)
  @NonNull
  public String getFullName() { return this.fullName; }
  
  public void setFullName(String name) {
    if (Util.fixEmptyAndTrim(name) == null)
      name = this.id; 
    this.fullName = name;
  }
  
  @Exported
  @CheckForNull
  public String getDescription() { return this.description; }
  
  public void setDescription(String description) { this.description = description; }
  
  public Map<Descriptor<UserProperty>, UserProperty> getProperties() { return Descriptor.toMap(this.properties); }
  
  public void addProperty(@NonNull UserProperty p) throws IOException {
    UserProperty old = getProperty(p.getClass());
    List<UserProperty> ps = new ArrayList<UserProperty>(this.properties);
    if (old != null)
      ps.remove(old); 
    ps.add(p);
    p.setUser(this);
    this.properties = ps;
    save();
  }
  
  @Exported(name = "property", inline = true)
  public List<UserProperty> getAllProperties() {
    if (hasPermission(Jenkins.ADMINISTER))
      return Collections.unmodifiableList(this.properties); 
    return Collections.emptyList();
  }
  
  public <T extends UserProperty> T getProperty(Class<T> clazz) {
    for (UserProperty p : this.properties) {
      if (clazz.isInstance(p))
        return (T)(UserProperty)clazz.cast(p); 
    } 
    return null;
  }
  
  @NonNull
  public Authentication impersonate2() throws UsernameNotFoundException { return impersonate(getUserDetailsForImpersonation2()); }
  
  @Deprecated
  @NonNull
  public Authentication impersonate() throws UsernameNotFoundException {
    try {
      return Authentication.fromSpring(impersonate2());
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  @NonNull
  public UserDetails getUserDetailsForImpersonation2() throws UsernameNotFoundException {
    ImpersonatingUserDetailsService2 userDetailsService = new ImpersonatingUserDetailsService2((Jenkins.get().getSecurityRealm().getSecurityComponents()).userDetails2);
    try {
      UserDetails userDetails = userDetailsService.loadUserByUsername(this.id);
      LOGGER.log(Level.FINE, "Impersonation of the user {0} was a success", this.id);
      return userDetails;
    } catch (UserMayOrMayNotExistException2 e) {
      LOGGER.log(Level.FINE, "The user {0} may or may not exist in the SecurityRealm, so we provide minimum access", this.id);
    } catch (UsernameNotFoundException e) {
      if (ALLOW_NON_EXISTENT_USER_TO_LOGIN) {
        LOGGER.log(Level.FINE, "The user {0} was not found in the SecurityRealm but we are required to let it pass, due to ALLOW_NON_EXISTENT_USER_TO_LOGIN", this.id);
      } else {
        LOGGER.log(Level.FINE, "The user {0} was not found in the SecurityRealm", this.id);
        throw e;
      } 
    } 
    return new LegitimateButUnknownUserDetails(this.id);
  }
  
  @Deprecated
  @NonNull
  public UserDetails getUserDetailsForImpersonation() throws UsernameNotFoundException {
    try {
      return UserDetails.fromSpring(getUserDetailsForImpersonation2());
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public Authentication impersonate(@NonNull UserDetails userDetails) { return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), "", userDetails.getAuthorities()); }
  
  @RequirePOST
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException {
    checkPermission(Jenkins.ADMINISTER);
    this.description = req.getParameter("description");
    save();
    rsp.sendRedirect(".");
  }
  
  @NonNull
  public static User getUnknown() { return getById("unknown", true); }
  
  @Deprecated
  @Nullable
  public static User get(String idOrFullName, boolean create) { return get(idOrFullName, create, Collections.emptyMap()); }
  
  @Nullable
  public static User get(String idOrFullName, boolean create, @NonNull Map context) {
    if (idOrFullName == null)
      return null; 
    User user = AllUsers.get(idOrFullName);
    if (user != null)
      return user; 
    String id = CanonicalIdResolver.resolve(idOrFullName, context);
    return getOrCreateById(id, idOrFullName, create);
  }
  
  @Nullable
  private static User getOrCreateById(@NonNull String id, @NonNull String fullName, boolean create) {
    User u = AllUsers.get(id);
    if (u == null && (create || UserIdMapper.getInstance().isMapped(id))) {
      u = new User(id, fullName);
      AllUsers.put(id, u);
      if (!id.equals(fullName) && !UserIdMapper.getInstance().isMapped(id))
        try {
          u.save();
        } catch (IOException x) {
          LOGGER.log(Level.WARNING, "Failed to save user configuration for " + id, x);
        }  
    } 
    return u;
  }
  
  @Deprecated
  @NonNull
  public static User get(String idOrFullName) { return getOrCreateByIdOrFullName(idOrFullName); }
  
  @NonNull
  public static User getOrCreateByIdOrFullName(@NonNull String idOrFullName) { return get(idOrFullName, true, Collections.emptyMap()); }
  
  @CheckForNull
  public static User current() { return get2(Jenkins.getAuthentication2()); }
  
  @CheckForNull
  public static User get2(@CheckForNull Authentication a) {
    if (a == null || a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
      return null; 
    return getById(a.getName(), true);
  }
  
  @Deprecated
  @CheckForNull
  public static User get(@CheckForNull Authentication a) { return get2((a != null) ? a.toSpring() : null); }
  
  @Nullable
  public static User getById(String id, boolean create) { return getOrCreateById(id, id, create); }
  
  @NonNull
  public static Collection<User> getAll() {
    strategy = idStrategy();
    ArrayList<User> users = new ArrayList<User>(AllUsers.values());
    users.sort((o1, o2) -> strategy.compare(o1.getId(), o2.getId()));
    return users;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void reload() {
    UserIdMapper.getInstance().reload();
    AllUsers.reload();
  }
  
  public static void rekey() {
    try {
      reload();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to perform rekey operation.", e);
    } 
  }
  
  @NonNull
  public String getDisplayName() { return getFullName(); }
  
  private boolean relatedTo(@NonNull Run<?, ?> b) {
    if (b instanceof RunWithSCM && ((RunWithSCM)b).hasParticipant(this))
      return true; 
    for (Cause cause : b.getCauses()) {
      if (cause instanceof Cause.UserIdCause) {
        String userId = ((Cause.UserIdCause)cause).getUserId();
        if (userId != null && idStrategy().equals(userId, getId()))
          return true; 
      } 
    } 
    return false;
  }
  
  @WithBridgeMethods({List.class})
  @NonNull
  public RunList getBuilds() {
    return RunList.fromJobs(Jenkins.get()
        .allItems(Job.class)).filter(this::relatedTo);
  }
  
  @NonNull
  public Set<AbstractProject<?, ?>> getProjects() {
    Set<AbstractProject<?, ?>> r = new HashSet<AbstractProject<?, ?>>();
    for (AbstractProject<?, ?> p : Jenkins.get().allItems(AbstractProject.class, p -> p.hasParticipant(this)))
      r.add(p); 
    return r;
  }
  
  public String toString() { return this.id; }
  
  @Deprecated
  public static void clear() {
    if (ExtensionList.lookup(AllUsers.class).isEmpty())
      return; 
    UserIdMapper.getInstance().clear();
    AllUsers.clear();
  }
  
  private static File getConfigFileFor(String id) { return new File(getUserFolderFor(id), "config.xml"); }
  
  private static File getUserFolderFor(String id) { return new File(getRootDir(), idStrategy().filenameOf(id)); }
  
  @CheckForNull
  public File getUserFolder() { return getExistingUserFolder(); }
  
  @CheckForNull
  private File getExistingUserFolder() { return UserIdMapper.getInstance().getDirectory(this.id); }
  
  static File getRootDir() { return new File(Jenkins.get().getRootDir(), "users"); }
  
  public static boolean isIdOrFullnameAllowed(@CheckForNull String id) {
    if (StringUtils.isBlank(id))
      return false; 
    String trimmedId = id.trim();
    for (String invalidId : ILLEGAL_PERSISTED_USERNAMES) {
      if (trimmedId.equalsIgnoreCase(invalidId))
        return false; 
    } 
    return true;
  }
  
  public void save() {
    if (!isIdOrFullnameAllowed(this.id))
      throw FormValidation.error(Messages.User_IllegalUsername(this.id)); 
    if (!isIdOrFullnameAllowed(this.fullName))
      throw FormValidation.error(Messages.User_IllegalFullname(this.fullName)); 
    if (BulkChange.contains(this))
      return; 
    XmlFile xmlFile = new XmlFile(XSTREAM, constructUserConfigFile());
    xmlFile.write(this);
    SaveableListener.fireOnChange(this, xmlFile);
  }
  
  private File constructUserConfigFile() { return new File(putUserFolderIfAbsent(), "config.xml"); }
  
  private File putUserFolderIfAbsent() { return UserIdMapper.getInstance().putIfAbsent(this.id, true); }
  
  public void delete() {
    String idKey = idStrategy().keyFor(this.id);
    File existingUserFolder = getExistingUserFolder();
    UserIdMapper.getInstance().remove(this.id);
    AllUsers.remove(this.id);
    deleteExistingUserFolder(existingUserFolder);
    UserDetailsCache.get().invalidate(idKey);
  }
  
  private void deleteExistingUserFolder(File existingUserFolder) throws IOException {
    if (existingUserFolder != null && existingUserFolder.exists())
      Util.deleteRecursive(existingUserFolder); 
  }
  
  public Api getApi() { return new Api(this); }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException {
    checkPermission(Jenkins.ADMINISTER);
    JSONObject json = req.getSubmittedForm();
    String oldFullName = this.fullName;
    this.fullName = json.getString("fullName");
    this.description = json.getString("description");
    List<UserProperty> props = new ArrayList<UserProperty>();
    int i = 0;
    for (UserPropertyDescriptor d : UserProperty.all()) {
      UserProperty p = getProperty(d.clazz);
      JSONObject o = json.optJSONObject("userProperty" + i++);
      if (o != null)
        if (p != null) {
          p = p.reconfigure(req, o);
        } else {
          p = (UserProperty)d.newInstance(req, o);
        }  
      if (p != null) {
        p.setUser(this);
        props.add(p);
      } 
    } 
    this.properties = props;
    save();
    if (oldFullName != null && !oldFullName.equals(this.fullName))
      UserDetailsCache.get().invalidate(oldFullName); 
    FormApply.success(".").generateResponse(req, rsp, this);
  }
  
  @RequirePOST
  public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException {
    checkPermission(Jenkins.ADMINISTER);
    if (idStrategy().equals(this.id, Jenkins.getAuthentication2().getName())) {
      rsp.sendError(400, "Cannot delete self");
      return;
    } 
    delete();
    rsp.sendRedirect2("../..");
  }
  
  public void doRssAll(StaplerRequest req, StaplerResponse rsp) throws IOException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (all builds)", getUrl(), getBuilds().newBuilds()); }
  
  public void doRssFailed(StaplerRequest req, StaplerResponse rsp) throws IOException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (failed builds)", getUrl(), getBuilds().regressionOnly()); }
  
  public void doRssLatest(StaplerRequest req, StaplerResponse rsp) throws IOException {
    List<Run> lastBuilds = new ArrayList<Run>();
    for (Job<?, ?> p : Jenkins.get().allItems(Job.class)) {
      for (Run<?, ?> b = p.getLastBuild(); b != null; b = b.getPreviousBuild()) {
        if (relatedTo(b)) {
          lastBuilds.add(b);
          break;
        } 
      } 
    } 
    lastBuilds.sort((o1, o2) -> Items.BY_FULL_NAME.compare(o1.getParent(), o2.getParent()));
    RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (latest builds)", getUrl(), RunList.fromRuns(lastBuilds), Run.FEED_ADAPTER_LATEST);
  }
  
  @NonNull
  public ACL getACL() {
    ACL base = Jenkins.get().getAuthorizationStrategy().getACL(this);
    return ACL.lambda2((a, permission) -> Boolean.valueOf(((idStrategy().equals(a.getName(), this.id) && !(a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) || base
          .hasPermission2(a, permission))));
  }
  
  public boolean canDelete() {
    IdStrategy strategy = idStrategy();
    return (hasPermission(Jenkins.ADMINISTER) && !strategy.equals(this.id, Jenkins.getAuthentication2().getName()) && 
      UserIdMapper.getInstance().isMapped(this.id));
  }
  
  @NonNull
  public List<String> getAuthorities() {
    Authentication authentication;
    if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER))
      return Collections.emptyList(); 
    List<String> r = new ArrayList<String>();
    try {
      authentication = impersonate2();
    } catch (UsernameNotFoundException x) {
      LOGGER.log(Level.FINE, "cannot look up authorities for " + this.id, x);
      return Collections.emptyList();
    } 
    for (GrantedAuthority a : authentication.getAuthorities()) {
      if (a.equals(SecurityRealm.AUTHENTICATED_AUTHORITY2))
        continue; 
      String n = a.getAuthority();
      if (n != null && !idStrategy().equals(n, this.id))
        r.add(n); 
    } 
    r.sort(String.CASE_INSENSITIVE_ORDER);
    return r;
  }
  
  public Object getDynamic(String token) {
    for (Action action : getTransientActions()) {
      if (Objects.equals(action.getUrlName(), token))
        return action; 
    } 
    for (Action action : getPropertyActions()) {
      if (Objects.equals(action.getUrlName(), token))
        return action; 
    } 
    return null;
  }
  
  public List<Action> getPropertyActions() {
    List<Action> actions = new ArrayList<Action>();
    for (UserProperty userProp : getProperties().values()) {
      if (userProp instanceof Action)
        actions.add((Action)userProp); 
    } 
    return Collections.unmodifiableList(actions);
  }
  
  public List<Action> getTransientActions() {
    List<Action> actions = new ArrayList<Action>();
    for (TransientUserActionFactory factory : TransientUserActionFactory.all())
      actions.addAll(factory.createFor(this)); 
    return Collections.unmodifiableList(actions);
  }
  
  public ModelObjectWithContextMenu.ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception { return (new ModelObjectWithContextMenu.ContextMenu()).from(this, request, response); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK && 
      !Jenkins.get().hasPermission(Jenkins.READ))
      return null; 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  static Set<String> getIllegalPersistedUsernames() { return new HashSet(Arrays.asList(ILLEGAL_PERSISTED_USERNAMES)); }
  
  private Object writeReplace() { return XmlFile.replaceIfNotAtTopLevel(this, () -> new Replacer(this)); }
}
