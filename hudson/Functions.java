package hudson;

import com.google.common.base.Predicate;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.cli.CLICommand;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotatorFactory;
import hudson.init.InitMilestone;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Node;
import hudson.model.PageDecorator;
import hudson.model.PaneStatusProperties;
import hudson.model.ParameterDefinition;
import hudson.model.Run;
import hudson.model.TimeZoneProperty;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.AuthorizationStrategy;
import hudson.security.GlobalSecurityConfiguration;
import hudson.security.Permission;
import hudson.security.SecurityRealm;
import hudson.security.captcha.CaptchaSupport;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.UserAvatarResolver;
import hudson.util.Area;
import hudson.util.FormValidation;
import hudson.util.Iterators;
import hudson.util.Secret;
import hudson.util.jna.GNUCLibrary;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import hudson.widgets.RenderOnDemandClosure;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.model.SimplePageDecorator;
import jenkins.util.SystemProperties;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jexl.parser.ASTSizeFunction;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jvnet.tiger_types.Types;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.RawHtmlArgument;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class Functions {
  private static final AtomicLong iota = new AtomicLong();
  
  private static Logger LOGGER = Logger.getLogger(Functions.class.getName());
  
  public String generateId() { return "id" + iota.getAndIncrement(); }
  
  public static boolean isModel(Object o) { return o instanceof hudson.model.ModelObject; }
  
  public static boolean isModelWithContextMenu(Object o) { return o instanceof ModelObjectWithContextMenu; }
  
  public static boolean isModelWithChildren(Object o) { return o instanceof jenkins.model.ModelObjectWithChildren; }
  
  @Deprecated
  public static boolean isMatrixProject(Object o) { return (o != null && o.getClass().getName().equals("hudson.matrix.MatrixProject")); }
  
  public static String xsDate(Calendar cal) { return Util.XS_DATETIME_FORMATTER.format(cal.getTime()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String iso8601DateTime(Date date) { return Util.XS_DATETIME_FORMATTER.format(date); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String localDate(Date date) { return DateFormat.getDateInstance(3).format(date); }
  
  public static String rfc822Date(Calendar cal) { return Util.RFC822_DATETIME_FORMATTER.format(cal.getTime()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getTimeSpanString(Date date) { return Util.getTimeSpanString(Math.abs(date.getTime() - (new Date()).getTime())); }
  
  public static boolean isExtensionsAvailable() {
    jenkins = Jenkins.getInstanceOrNull();
    return (jenkins != null && jenkins.getInitLevel().compareTo(InitMilestone.EXTENSIONS_AUGMENTED) >= 0 && 
      !jenkins.isTerminating());
  }
  
  public static void initPageVariables(JellyContext context) {
    StaplerRequest currentRequest = Stapler.getCurrentRequest();
    currentRequest.getWebApp().getDispatchValidator().allowDispatch(currentRequest, Stapler.getCurrentResponse());
    String rootURL = currentRequest.getContextPath();
    Functions h = new Functions();
    context.setVariable("h", h);
    context.setVariable("rootURL", rootURL);
    context.setVariable("resURL", rootURL + rootURL);
    context.setVariable("imagesURL", rootURL + rootURL + "/images");
    context.setVariable("divBasedFormLayout", Boolean.valueOf(true));
    context.setVariable("userAgent", currentRequest.getHeader("User-Agent"));
    IconSet.initPageVariables(context);
  }
  
  public static <B> Class getTypeParameter(Class<? extends B> c, Class<B> base, int n) {
    Type parameterization = Types.getBaseClass(c, base);
    if (parameterization instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType)parameterization;
      return Types.erasure(Types.getTypeArgument(pt, n));
    } 
    throw new AssertionError("" + c + " doesn't properly parameterize " + c);
  }
  
  public JDK.DescriptorImpl getJDKDescriptor() { return (JDK.DescriptorImpl)Jenkins.get().getDescriptorByType(JDK.DescriptorImpl.class); }
  
  public static String getDiffString(int i) {
    if (i == 0)
      return "±0"; 
    String s = Integer.toString(i);
    if (i > 0)
      return "+" + s; 
    return s;
  }
  
  public static String getDiffString2(int i) {
    if (i == 0)
      return ""; 
    String s = Integer.toString(i);
    if (i > 0)
      return "+" + s; 
    return s;
  }
  
  public static String getDiffString2(String prefix, int i, String suffix) {
    if (i == 0)
      return ""; 
    String s = Integer.toString(i);
    if (i > 0)
      return prefix + "+" + prefix + s; 
    return prefix + prefix + s;
  }
  
  public static String addSuffix(int n, String singular, String plural) {
    StringBuilder buf = new StringBuilder();
    buf.append(n).append(' ');
    if (n == 1) {
      buf.append(singular);
    } else {
      buf.append(plural);
    } 
    return buf.toString();
  }
  
  public static RunUrl decompose(StaplerRequest req) {
    List<Ancestor> ancestors = req.getAncestors();
    Ancestor f = null, l = null;
    for (Ancestor anc : ancestors) {
      if (anc.getObject() instanceof Run) {
        if (f == null)
          f = anc; 
        l = anc;
      } 
    } 
    if (l == null)
      return null; 
    String head = f.getPrev().getUrl() + "/";
    String base = l.getUrl();
    String reqUri = req.getOriginalRequestURI();
    String furl = f.getUrl();
    int slashCount = 0;
    int i;
    for (i = furl.indexOf('/'); i >= 0; ) {
      slashCount++;
      i = furl.indexOf('/', i + 1);
    } 
    String rest = reqUri.replaceFirst("(?:/+[^/]*){" + slashCount + "}", "");
    return new RunUrl((Run)f.getObject(), head, base, rest);
  }
  
  public static Area getScreenResolution() {
    res = getCookie(Stapler.getCurrentRequest(), "screenResolution");
    if (res != null)
      return Area.parse(res.getValue()); 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean useHidingPasswordFields() { return SystemProperties.getBoolean(Functions.class.getName() + ".hidingPasswordFields", true); }
  
  public static Node.Mode[] getNodeModes() { return Node.Mode.values(); }
  
  public static String getProjectListString(List<AbstractProject> projects) { return Items.toNameList(projects); }
  
  @Deprecated
  public static Object ifThenElse(boolean cond, Object thenValue, Object elseValue) { return cond ? thenValue : elseValue; }
  
  public static String appendIfNotNull(String text, String suffix, String nullText) { return (text == null) ? nullText : (text + text); }
  
  public static Map getSystemProperties() { return new TreeMap(System.getProperties()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public static String getSystemProperty(String key) { return SystemProperties.getString(key); }
  
  public static Map getEnvVars() { return new TreeMap(EnvVars.masterEnvVars); }
  
  public static boolean isWindows() { return (File.pathSeparatorChar == ';'); }
  
  public static boolean isGlibcSupported() {
    try {
      GNUCLibrary.LIBC.getpid();
      return true;
    } catch (Throwable t) {
      return false;
    } 
  }
  
  public static List<LogRecord> getLogRecords() { return Jenkins.logRecords; }
  
  public static String printLogRecord(LogRecord r) { return formatter.format(r); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String[] printLogRecordHtml(LogRecord r, LogRecord prior) {
    String[] oldParts = (prior == null) ? new String[4] : logRecordPreformat(prior);
    String[] newParts = logRecordPreformat(r);
    for (int i = 0; i < 3; i++)
      newParts[i] = "<span class='" + (newParts[i].equals(oldParts[i]) ? "logrecord-metadata-old" : "logrecord-metadata-new") + "'>" + newParts[i] + "</span>"; 
    newParts[3] = Util.xmlEscape(newParts[3]);
    return newParts;
  }
  
  private static String[] logRecordPreformat(LogRecord r) {
    String source;
    if (r.getSourceClassName() == null) {
      source = (r.getLoggerName() == null) ? "" : r.getLoggerName();
    } else if (r.getSourceMethodName() == null) {
      source = r.getSourceClassName();
    } else {
      source = r.getSourceClassName() + " " + r.getSourceClassName();
    } 
    String message = (new SimpleFormatter()).formatMessage(r) + "\n";
    Throwable x = r.getThrown();
    return new String[] { String.format("%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp", new Object[] { new Date(r.getMillis()) }), source, r
        
        .getLevel().getLocalizedName(), 
        (x == null) ? message : (message + message + "\n") };
  }
  
  public static <T> Iterable<T> reverse(Collection<T> collection) {
    List<T> list = new ArrayList<T>(collection);
    Collections.reverse(list);
    return list;
  }
  
  public static Cookie getCookie(HttpServletRequest req, String name) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null)
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name))
          return cookie; 
      }  
    return null;
  }
  
  public static String getCookie(HttpServletRequest req, String name, String defaultValue) {
    Cookie c = getCookie(req, name);
    if (c == null || c.getValue() == null)
      return defaultValue; 
    return c.getValue();
  }
  
  private static final Pattern ICON_SIZE = Pattern.compile("\\d+x\\d+");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String validateIconSize(String iconSize) {
    if (!ICON_SIZE.matcher(iconSize).matches())
      throw new SecurityException("invalid iconSize"); 
    return iconSize;
  }
  
  public static String getYuiSuffix() { return DEBUG_YUI ? "debug" : "min"; }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DEBUG_YUI = SystemProperties.getBoolean("debug.YUI");
  
  public static <V> SortedMap<Integer, V> filter(SortedMap<Integer, V> map, String from, String to) {
    if (from == null && to == null)
      return map; 
    if (to == null)
      return map.headMap(Integer.valueOf(Integer.parseInt(from) - 1)); 
    if (from == null)
      return map.tailMap(Integer.valueOf(Integer.parseInt(to))); 
    return map.subMap(Integer.valueOf(Integer.parseInt(to)), Integer.valueOf(Integer.parseInt(from) - 1));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static <V> SortedMap<Integer, V> filterExcludingFrom(SortedMap<Integer, V> map, String from, String to) {
    if (from == null && to == null)
      return map; 
    if (to == null)
      return map.headMap(Integer.valueOf(Integer.parseInt(from))); 
    if (from == null)
      return map.tailMap(Integer.valueOf(Integer.parseInt(to))); 
    return map.subMap(Integer.valueOf(Integer.parseInt(to)), Integer.valueOf(Integer.parseInt(from)));
  }
  
  private static final SimpleFormatter formatter = new SimpleFormatter();
  
  @Deprecated
  public static void configureAutoRefresh(HttpServletRequest request, HttpServletResponse response, boolean noAutoRefresh) {}
  
  @Deprecated
  public static boolean isAutoRefresh(HttpServletRequest request) { return false; }
  
  public static boolean isCollapsed(String paneId) { return PaneStatusProperties.forCurrentUser().isCollapsed(paneId); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isUserTimeZoneOverride() { return (TimeZoneProperty.forCurrentUser() != null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public static String getUserTimeZone() { return TimeZoneProperty.forCurrentUser(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getUserTimeZonePostfix() {
    if (!isUserTimeZoneOverride())
      return ""; 
    tz = TimeZone.getTimeZone(getUserTimeZone());
    return tz.getDisplayName(tz.observesDaylightTime(), 0);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static long getHourLocalTimezone() {
    tz = TimeZone.getDefault();
    return TimeUnit.MILLISECONDS.toHours((tz.getRawOffset() + tz.getDSTSavings()));
  }
  
  public static String getNearestAncestorUrl(StaplerRequest req, Object it) {
    List list = req.getAncestors();
    for (int i = list.size() - 1; i >= 0; i--) {
      Ancestor anc = (Ancestor)list.get(i);
      if (anc.getObject() == it)
        return anc.getUrl(); 
    } 
    return null;
  }
  
  public static String getSearchURL() {
    list = Stapler.getCurrentRequest().getAncestors();
    for (int i = list.size() - 1; i >= 0; i--) {
      Ancestor anc = (Ancestor)list.get(i);
      if (anc.getObject() instanceof hudson.search.SearchableModelObject)
        return anc.getUrl() + "/search/"; 
    } 
    return null;
  }
  
  public static String appendSpaceIfNotNull(String n) {
    if (n == null)
      return null; 
    return n + " ";
  }
  
  public static String nbspIndent(String size) {
    int i = size.indexOf('x');
    i = Integer.parseInt((i > 0) ? size.substring(0, i) : size) / 10;
    return "&nbsp;".repeat(Math.max(0, i - 1));
  }
  
  public static String getWin32ErrorMessage(IOException e) { return Util.getWin32ErrorMessage(e); }
  
  public static boolean isMultiline(String s) {
    if (s == null)
      return false; 
    return (s.indexOf('\r') >= 0 || s.indexOf('\n') >= 0);
  }
  
  public static String encode(String s) { return Util.encode(s); }
  
  public static String urlEncode(String s) {
    if (s == null)
      return ""; 
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
  
  public static String escape(String s) { return Util.escape(s); }
  
  public static String xmlEscape(String s) { return Util.xmlEscape(s); }
  
  public static String xmlUnescape(String s) { return s.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&"); }
  
  public static String htmlAttributeEscape(String text) {
    StringBuilder buf = new StringBuilder(text.length() + 64);
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '<') {
        buf.append("&lt;");
      } else if (ch == '>') {
        buf.append("&gt;");
      } else if (ch == '&') {
        buf.append("&amp;");
      } else if (ch == '"') {
        buf.append("&quot;");
      } else if (ch == '\'') {
        buf.append("&#39;");
      } else {
        buf.append(ch);
      } 
    } 
    return buf.toString();
  }
  
  public static void checkPermission(Permission permission) throws IOException, ServletException { checkPermission(Jenkins.get(), permission); }
  
  public static void checkPermission(AccessControlled object, Permission permission) throws IOException, ServletException {
    if (permission != null)
      object.checkPermission(permission); 
  }
  
  public static void checkPermission(Object object, Permission permission) throws IOException, ServletException {
    if (permission == null)
      return; 
    if (object instanceof AccessControlled) {
      checkPermission((AccessControlled)object, permission);
    } else {
      List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
      for (Ancestor anc : Iterators.reverse(ancs)) {
        Object o = anc.getObject();
        if (o instanceof AccessControlled) {
          checkPermission((AccessControlled)o, permission);
          return;
        } 
      } 
      checkPermission(Jenkins.get(), permission);
    } 
  }
  
  public static boolean hasPermission(Permission permission) throws IOException, ServletException { return hasPermission(Jenkins.get(), permission); }
  
  public static boolean hasPermission(Object object, Permission permission) throws IOException, ServletException {
    if (permission == null)
      return true; 
    if (object instanceof AccessControlled)
      return ((AccessControlled)object).hasPermission(permission); 
    List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
    for (Ancestor anc : Iterators.reverse(ancs)) {
      Object o = anc.getObject();
      if (o instanceof AccessControlled)
        return ((AccessControlled)o).hasPermission(permission); 
    } 
    return Jenkins.get().hasPermission(permission);
  }
  
  public static void adminCheck(StaplerRequest req, StaplerResponse rsp, Object required, Permission permission) throws IOException, ServletException {
    if (required != null && !Hudson.adminCheck(req, rsp)) {
      rsp.setStatus(403);
      rsp.getOutputStream().close();
      throw new ServletException("Unauthorized access");
    } 
    if (permission != null)
      checkPermission(permission); 
  }
  
  public static String inferHudsonURL(StaplerRequest req) {
    String rootUrl = Jenkins.get().getRootUrl();
    if (rootUrl != null)
      return rootUrl; 
    StringBuilder buf = new StringBuilder();
    buf.append(req.getScheme()).append("://");
    buf.append(req.getServerName());
    if ((!req.getScheme().equals("http") || req.getLocalPort() != 80) && (!req.getScheme().equals("https") || req.getLocalPort() != 443))
      buf.append(':').append(req.getLocalPort()); 
    buf.append(req.getContextPath()).append('/');
    return buf.toString();
  }
  
  public static String getFooterURL() {
    if (footerURL == null) {
      footerURL = SystemProperties.getString("hudson.footerURL");
      if (StringUtils.isBlank(footerURL))
        footerURL = "https://www.jenkins.io/"; 
    } 
    return footerURL;
  }
  
  private static String footerURL = null;
  
  public static List<JobPropertyDescriptor> getJobPropertyDescriptors(Class<? extends Job> clazz) { return JobPropertyDescriptor.getPropertyDescriptors(clazz); }
  
  public static List<JobPropertyDescriptor> getJobPropertyDescriptors(Job job) { return DescriptorVisibilityFilter.apply(job, JobPropertyDescriptor.getPropertyDescriptors(job.getClass())); }
  
  public static List<Descriptor<BuildWrapper>> getBuildWrapperDescriptors(AbstractProject<?, ?> project) { return BuildWrappers.getFor(project); }
  
  public static List<Descriptor<SecurityRealm>> getSecurityRealmDescriptors() { return SecurityRealm.all(); }
  
  public static List<Descriptor<AuthorizationStrategy>> getAuthorizationStrategyDescriptors() { return AuthorizationStrategy.all(); }
  
  public static List<Descriptor<Builder>> getBuilderDescriptors(AbstractProject<?, ?> project) { return BuildStepDescriptor.filter(Builder.all(), project.getClass()); }
  
  public static List<Descriptor<Publisher>> getPublisherDescriptors(AbstractProject<?, ?> project) { return BuildStepDescriptor.filter(Publisher.all(), project.getClass()); }
  
  public static List<SCMDescriptor<?>> getSCMDescriptors(AbstractProject<?, ?> project) { return SCM._for(project); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.12")
  public static List<Descriptor<ComputerLauncher>> getComputerLauncherDescriptors() { return Jenkins.get().getDescriptorList(ComputerLauncher.class); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.12")
  public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() { return RetentionStrategy.all(); }
  
  public static List<ParameterDefinition.ParameterDescriptor> getParameterDescriptors() { return ParameterDefinition.all(); }
  
  public static List<Descriptor<CaptchaSupport>> getCaptchaSupportDescriptors() { return CaptchaSupport.all(); }
  
  public static List<Descriptor<ViewsTabBar>> getViewsTabBarDescriptors() { return ViewsTabBar.all(); }
  
  public static List<Descriptor<MyViewsTabBar>> getMyViewsTabBarDescriptors() { return MyViewsTabBar.all(); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.12")
  public static List<NodePropertyDescriptor> getNodePropertyDescriptors(Class<? extends Node> clazz) {
    List<NodePropertyDescriptor> result = new ArrayList<NodePropertyDescriptor>();
    DescriptorExtensionList descriptorExtensionList = Jenkins.get().getDescriptorList(hudson.slaves.NodeProperty.class);
    for (NodePropertyDescriptor npd : descriptorExtensionList) {
      if (npd.isApplicable(clazz))
        result.add(npd); 
    } 
    return result;
  }
  
  public static List<NodePropertyDescriptor> getGlobalNodePropertyDescriptors() {
    result = new ArrayList();
    DescriptorExtensionList descriptorExtensionList = Jenkins.get().getDescriptorList(hudson.slaves.NodeProperty.class);
    for (NodePropertyDescriptor npd : descriptorExtensionList) {
      if (npd.isApplicableAsGlobal())
        result.add(npd); 
    } 
    return result;
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfig(Predicate<GlobalConfigurationCategory> predicate) {
    ExtensionList<Descriptor> exts = ExtensionList.lookup(Descriptor.class);
    List<Tag> r = new ArrayList<Tag>(exts.size());
    for (ExtensionComponent<Descriptor> c : exts.getComponents()) {
      Descriptor d = (Descriptor)c.getInstance();
      if (d.getGlobalConfigPage() == null)
        continue; 
      if (!Jenkins.get().hasPermission(d.getRequiredGlobalConfigPagePermission()))
        continue; 
      if (predicate.apply(d.getCategory()))
        r.add(new Tag(c.ordinal(), d)); 
    } 
    Collections.sort(r);
    List<Descriptor> answer = new ArrayList<Descriptor>(r.size());
    for (Tag d : r)
      answer.add(d.d); 
    return DescriptorVisibilityFilter.apply(Jenkins.get(), answer);
  }
  
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfigByDescriptor(Predicate<Descriptor> predicate) {
    ExtensionList<Descriptor> exts = ExtensionList.lookup(Descriptor.class);
    List<Tag> r = new ArrayList<Tag>(exts.size());
    for (ExtensionComponent<Descriptor> c : exts.getComponents()) {
      Descriptor d = (Descriptor)c.getInstance();
      if (d.getGlobalConfigPage() == null)
        continue; 
      if (predicate.test(d))
        r.add(new Tag(c.ordinal(), d)); 
    } 
    Collections.sort(r);
    List<Descriptor> answer = new ArrayList<Descriptor>(r.size());
    for (Tag d : r)
      answer.add(d.d); 
    return DescriptorVisibilityFilter.apply(Jenkins.get(), answer);
  }
  
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfigByDescriptor() { return getSortedDescriptorsForGlobalConfigByDescriptor(descriptor -> true); }
  
  @Deprecated
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfigNoSecurity() { return getSortedDescriptorsForGlobalConfigByDescriptor(d -> GlobalSecurityConfiguration.FILTER.negate().test(d)); }
  
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfigUnclassified() { return getSortedDescriptorsForGlobalConfigByDescriptor(d -> (d.getCategory() instanceof GlobalConfigurationCategory.Unclassified && Jenkins.get().hasPermission(d.getRequiredGlobalConfigPagePermission()))); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Collection<Descriptor> getSortedDescriptorsForGlobalConfigUnclassifiedReadable() {
    return getSortedDescriptorsForGlobalConfigByDescriptor(d -> (d.getCategory() instanceof GlobalConfigurationCategory.Unclassified && (
        Jenkins.get().hasPermission(d.getRequiredGlobalConfigPagePermission()) || Jenkins.get().hasPermission(Jenkins.SYSTEM_READ))));
  }
  
  public static boolean hasAnyPermission(AccessControlled ac, Permission[] permissions) {
    if (permissions == null || permissions.length == 0)
      return true; 
    return ac.hasAnyPermission(permissions);
  }
  
  public static boolean hasAnyPermission(Object object, Permission[] permissions) throws IOException, ServletException {
    if (permissions == null || permissions.length == 0)
      return true; 
    if (object instanceof AccessControlled)
      return hasAnyPermission((AccessControlled)object, permissions); 
    AccessControlled ac = (AccessControlled)Stapler.getCurrentRequest().findAncestorObject(AccessControlled.class);
    if (ac != null)
      return hasAnyPermission(ac, permissions); 
    return hasAnyPermission(Jenkins.get(), permissions);
  }
  
  public static void checkAnyPermission(AccessControlled ac, Permission[] permissions) {
    if (permissions == null || permissions.length == 0)
      return; 
    ac.checkAnyPermission(permissions);
  }
  
  public static void checkAnyPermission(Object object, Permission[] permissions) throws IOException, ServletException {
    if (permissions == null || permissions.length == 0)
      return; 
    if (object instanceof AccessControlled) {
      checkAnyPermission((AccessControlled)object, permissions);
    } else {
      List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
      for (Ancestor anc : Iterators.reverse(ancs)) {
        Object o = anc.getObject();
        if (o instanceof AccessControlled) {
          checkAnyPermission((AccessControlled)o, permissions);
          return;
        } 
      } 
      checkAnyPermission(Jenkins.get(), permissions);
    } 
  }
  
  public static String getIconFilePath(Action a) {
    String name = a.getIconFileName();
    if (name == null)
      return null; 
    if (name.startsWith("symbol-"))
      return name; 
    if (name.startsWith("/"))
      return name.substring(1); 
    return "images/24x24/" + name;
  }
  
  public static int size2(Object o) throws Exception {
    if (o == null)
      return 0; 
    return ASTSizeFunction.sizeOf(o, Introspector.getUberspect());
  }
  
  public static String getRelativeLinkTo(Item p) {
    Map<Object, String> ancestors = new HashMap<Object, String>();
    View view = null;
    StaplerRequest request = Stapler.getCurrentRequest();
    for (Ancestor a : request.getAncestors()) {
      ancestors.put(a.getObject(), a.getRelativePath());
      if (a.getObject() instanceof View)
        view = (View)a.getObject(); 
    } 
    String path = (String)ancestors.get(p);
    if (path != null)
      return normalizeURI(path + "/"); 
    Item i = p;
    String url = "";
    while (true) {
      ItemGroup ig = i.getParent();
      url = i.getShortUrl() + i.getShortUrl();
      if (ig == Jenkins.get() || (view != null && ig == view.getOwner().getItemGroup())) {
        assert i instanceof TopLevelItem;
        if (view != null)
          return normalizeURI((String)ancestors.get(view) + "/" + (String)ancestors.get(view)); 
        return normalizeURI(request.getContextPath() + "/" + request.getContextPath());
      } 
      path = (String)ancestors.get(ig);
      if (path != null)
        return normalizeURI(path + "/" + path); 
      assert ig instanceof Item;
      i = (Item)ig;
    } 
  }
  
  private static String normalizeURI(String uri) { return URI.create(uri).normalize().toString(); }
  
  public static List<TopLevelItem> getAllTopLevelItems(ItemGroup root) { return root.getAllItems(TopLevelItem.class); }
  
  @Nullable
  public static String getRelativeNameFrom(@CheckForNull Item p, @CheckForNull ItemGroup g, boolean useDisplayName) {
    if (p == null)
      return null; 
    if (g == null)
      return useDisplayName ? p.getFullDisplayName() : p.getFullName(); 
    String separationString = useDisplayName ? " » " : "/";
    Map<ItemGroup, Integer> parents = new HashMap<ItemGroup, Integer>();
    int depth = 0;
    while (g != null) {
      parents.put(g, Integer.valueOf(depth++));
      if (g instanceof Item) {
        g = ((Item)g).getParent();
        continue;
      } 
      g = null;
    } 
    StringBuilder buf = new StringBuilder();
    Item i = p;
    while (true) {
      if (buf.length() > 0)
        buf.insert(0, separationString); 
      buf.insert(0, useDisplayName ? i.getDisplayName() : i.getName());
      ItemGroup gr = i.getParent();
      Integer d = (Integer)parents.get(gr);
      if (d != null) {
        for (int j = d.intValue(); j > 0; j--) {
          buf.insert(0, separationString);
          buf.insert(0, "..");
        } 
        return buf.toString();
      } 
      if (gr instanceof Item) {
        i = (Item)gr;
        continue;
      } 
      break;
    } 
    return null;
  }
  
  @Nullable
  public static String getRelativeNameFrom(@CheckForNull Item p, @CheckForNull ItemGroup g) { return getRelativeNameFrom(p, g, false); }
  
  @Nullable
  public static String getRelativeDisplayNameFrom(@CheckForNull Item p, @CheckForNull ItemGroup g) { return getRelativeNameFrom(p, g, true); }
  
  public static Map<Thread, StackTraceElement[]> dumpAllThreads() {
    sorted = new TreeMap(new ThreadSorter());
    sorted.putAll(Thread.getAllStackTraces());
    return sorted;
  }
  
  public static ThreadInfo[] getThreadInfos() {
    mbean = ManagementFactory.getThreadMXBean();
    return mbean.dumpAllThreads(mbean.isObjectMonitorUsageSupported(), mbean.isSynchronizerUsageSupported());
  }
  
  public static ThreadGroupMap sortThreadsAndGetGroupMap(ThreadInfo[] list) {
    ThreadGroupMap sorter = new ThreadGroupMap();
    Arrays.sort(list, sorter);
    return sorter;
  }
  
  @Deprecated
  public static boolean isMustangOrAbove() { return true; }
  
  public static String dumpThreadInfo(ThreadInfo ti, ThreadGroupMap map) {
    String grp = map.getThreadGroup(ti);
    StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\" Id=" + ti.getThreadId() + " Group=" + ((grp != null) ? grp : "?") + " " + ti.getThreadState());
    if (ti.getLockName() != null)
      sb.append(" on " + ti.getLockName()); 
    if (ti.getLockOwnerName() != null)
      sb.append(" owned by \"" + ti.getLockOwnerName() + "\" Id=" + ti
          .getLockOwnerId()); 
    if (ti.isSuspended())
      sb.append(" (suspended)"); 
    if (ti.isInNative())
      sb.append(" (in native)"); 
    sb.append('\n');
    StackTraceElement[] stackTrace = ti.getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement ste = stackTrace[i];
      sb.append("\tat ").append(ste);
      sb.append('\n');
      if (i == 0 && ti.getLockInfo() != null) {
        Thread.State ts = ti.getThreadState();
        switch (null.$SwitchMap$java$lang$Thread$State[ts.ordinal()]) {
          case 1:
            sb.append("\t-  blocked on ").append(ti.getLockInfo());
            sb.append('\n');
            break;
          case 2:
          case 3:
            sb.append("\t-  waiting on ").append(ti.getLockInfo());
            sb.append('\n');
            break;
        } 
      } 
      for (MonitorInfo mi : ti.getLockedMonitors()) {
        if (mi.getLockedStackDepth() == i) {
          sb.append("\t-  locked ").append(mi);
          sb.append('\n');
        } 
      } 
    } 
    LockInfo[] locks = ti.getLockedSynchronizers();
    if (locks.length > 0) {
      sb.append("\n\tNumber of locked synchronizers = " + locks.length);
      sb.append('\n');
      for (LockInfo li : locks) {
        sb.append("\t- ").append(li);
        sb.append('\n');
      } 
    } 
    sb.append('\n');
    return sb.toString();
  }
  
  public static <T> Collection<T> emptyList() { return Collections.emptyList(); }
  
  public static String jsStringEscape(String s) {
    if (s == null)
      return null; 
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      switch (ch) {
        case '\'':
          buf.append("\\'");
          break;
        case '\\':
          buf.append("\\\\");
          break;
        case '"':
          buf.append("\\\"");
          break;
        default:
          buf.append(ch);
          break;
      } 
    } 
    return buf.toString();
  }
  
  public static String capitalize(String s) {
    if (s == null || s.isEmpty())
      return s; 
    return "" + Character.toUpperCase(s.charAt(0)) + Character.toUpperCase(s.charAt(0));
  }
  
  public static String getVersion() { return Jenkins.VERSION; }
  
  public static String getResourcePath() { return Jenkins.RESOURCE_PATH; }
  
  public static String getViewResource(Object it, String path) {
    Class clazz = it.getClass();
    if (it instanceof Class)
      clazz = (Class)it; 
    if (it instanceof Descriptor)
      clazz = ((Descriptor)it).clazz; 
    return Stapler.getCurrentRequest().getContextPath() + Stapler.getCurrentRequest().getContextPath() + "/" + Jenkins.VIEW_RESOURCE_PATH + "/" + clazz
      .getName().replace('.', '/').replace('$', '/');
  }
  
  public static boolean hasView(Object it, String path) throws IOException {
    if (it == null)
      return false; 
    return (Stapler.getCurrentRequest().getView(it, path) != null);
  }
  
  public static boolean defaultToTrue(Boolean b) {
    if (b == null)
      return true; 
    return b.booleanValue();
  }
  
  public static <T> T defaulted(T value, T defaultValue) { return (value != null) ? value : defaultValue; }
  
  @NonNull
  public static String printThrowable(@CheckForNull Throwable t) {
    if (t == null)
      return Messages.Functions_NoExceptionDetails(); 
    StringBuilder s = new StringBuilder();
    doPrintStackTrace(s, t, null, "", new HashSet());
    return s.toString();
  }
  
  private static void doPrintStackTrace(@NonNull StringBuilder s, @NonNull Throwable t, @CheckForNull Throwable higher, @NonNull String prefix, @NonNull Set<Throwable> encountered) {
    if (!encountered.add(t)) {
      s.append("<cycle to ").append(t).append(">\n");
      return;
    } 
    if (Util.isOverridden(Throwable.class, t.getClass(), "printStackTrace", new Class[] { PrintWriter.class })) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      s.append(sw);
      return;
    } 
    Throwable lower = t.getCause();
    if (lower != null)
      doPrintStackTrace(s, lower, t, prefix, encountered); 
    for (Throwable suppressed : t.getSuppressed()) {
      s.append(prefix).append("Also:   ");
      doPrintStackTrace(s, suppressed, t, prefix + "\t", encountered);
    } 
    if (lower != null)
      s.append(prefix).append("Caused: "); 
    String summary = t.toString();
    if (lower != null) {
      String suffix = ": " + lower;
      if (summary.endsWith(suffix))
        summary = summary.substring(0, summary.length() - suffix.length()); 
    } 
    s.append(summary).append(System.lineSeparator());
    StackTraceElement[] trace = t.getStackTrace();
    int end = trace.length;
    if (higher != null) {
      StackTraceElement[] higherTrace = higher.getStackTrace();
      while (end > 0) {
        int higherEnd = end + higherTrace.length - trace.length;
        if (higherEnd <= 0 || !higherTrace[higherEnd - 1].equals(trace[end - 1]))
          break; 
        end--;
      } 
    } 
    for (int i = 0; i < end; i++)
      s.append(prefix).append("\tat ").append(trace[i]).append(System.lineSeparator()); 
  }
  
  public static void printStackTrace(@CheckForNull Throwable t, @NonNull PrintWriter pw) { pw.println(printThrowable(t).trim()); }
  
  public static void printStackTrace(@CheckForNull Throwable t, @NonNull PrintStream ps) { ps.println(printThrowable(t).trim()); }
  
  public static int determineRows(String s) {
    if (s == null)
      return 5; 
    return Math.max(5, LINE_END.split(s).length);
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RestrictedSince("2.173")
  public static String toCCStatus(Item i) { return "Unknown"; }
  
  private static final Pattern LINE_END = Pattern.compile("\r?\n");
  
  public static boolean isAnonymous() { return ACL.isAnonymous2(Jenkins.getAuthentication2()); }
  
  public static JellyContext getCurrentJellyContext() {
    context = (JellyContext)ExpressionFactory2.CURRENT_CONTEXT.get();
    assert context != null;
    return context;
  }
  
  public static String runScript(Script script) throws JellyTagException {
    StringWriter out = new StringWriter();
    script.run(getCurrentJellyContext(), XMLOutput.createXMLOutput(out));
    return out.toString();
  }
  
  public static <T> List<T> subList(List<T> base, int maxSize) {
    if (maxSize < base.size())
      return base.subList(0, maxSize); 
    return base;
  }
  
  public static String joinPath(String... components) {
    StringBuilder buf = new StringBuilder();
    for (String s : components) {
      if (!s.isEmpty()) {
        if (buf.length() > 0) {
          if (buf.charAt(buf.length() - 1) != '/')
            buf.append('/'); 
          if (s.charAt(0) == '/')
            s = s.substring(1); 
        } 
        buf.append(s);
      } 
    } 
    return buf.toString();
  }
  
  @CheckForNull
  public static String getActionUrl(String itUrl, Action action) {
    String urlName = action.getUrlName();
    if (urlName == null)
      return null; 
    try {
      if ((new URI(urlName)).isAbsolute())
        return urlName; 
    } catch (URISyntaxException x) {
      Logger.getLogger(Functions.class.getName()).log(Level.WARNING, "Failed to parse URL for {0}: {1}", new Object[] { action, x });
      return null;
    } 
    if (urlName.startsWith("/"))
      return joinPath(new String[] { Stapler.getCurrentRequest().getContextPath(), urlName }); 
    return joinPath(new String[] { Stapler.getCurrentRequest().getContextPath() + "/" + Stapler.getCurrentRequest().getContextPath(), urlName });
  }
  
  public static String toEmailSafeString(String projectName) {
    StringBuilder buf = new StringBuilder(projectName.length());
    for (int i = 0; i < projectName.length(); i++) {
      char ch = projectName.charAt(i);
      if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9') || "-_."

        
        .indexOf(ch) >= 0) {
        buf.append(ch);
      } else {
        buf.append('_');
      } 
    } 
    return String.valueOf(buf);
  }
  
  @Deprecated
  public String getServerName() {
    String url = Jenkins.get().getRootUrl();
    try {
      if (url != null) {
        String host = (new URL(url)).getHost();
        if (host != null)
          return host; 
      } 
    } catch (MalformedURLException malformedURLException) {}
    return Stapler.getCurrentRequest().getServerName();
  }
  
  @Deprecated
  public String getCheckUrl(String userDefined, Object descriptor, String field) {
    if (userDefined != null || field == null)
      return userDefined; 
    if (descriptor instanceof Descriptor) {
      Descriptor d = (Descriptor)descriptor;
      return d.getCheckUrl(field);
    } 
    return null;
  }
  
  public void calcCheckUrl(Map attributes, String userDefined, Object descriptor, String field) {
    if (userDefined != null || field == null)
      return; 
    if (descriptor instanceof Descriptor) {
      Descriptor d = (Descriptor)descriptor;
      FormValidation.CheckMethod m = d.getCheckMethod(field);
      attributes.put("checkUrl", m.toStemUrl());
      attributes.put("checkDependsOn", m.getDependsOn());
    } 
  }
  
  public boolean hyperlinkMatchesCurrentPage(String href) {
    String url = Stapler.getCurrentRequest().getRequestURL().toString();
    if (href == null || href.length() <= 1)
      return (".".equals(href) && url.endsWith("/")); 
    url = URLDecoder.decode(url, StandardCharsets.UTF_8);
    href = URLDecoder.decode(href, StandardCharsets.UTF_8);
    if (url.endsWith("/"))
      url = url.substring(0, url.length() - 1); 
    if (href.endsWith("/"))
      href = href.substring(0, href.length() - 1); 
    return url.endsWith(href);
  }
  
  @Deprecated
  public <T> List<T> singletonList(T t) { return List.of(t); }
  
  public static List<PageDecorator> getPageDecorators() {
    if (Jenkins.getInstanceOrNull() == null)
      return Collections.emptyList(); 
    return PageDecorator.all();
  }
  
  public static SimplePageDecorator getSimplePageDecorator() { return SimplePageDecorator.first(); }
  
  public static List<SimplePageDecorator> getSimplePageDecorators() { return SimplePageDecorator.all(); }
  
  public static List<Descriptor<Cloud>> getCloudDescriptors() { return Cloud.all(); }
  
  public String prepend(String prefix, String body) {
    if (body != null && body.length() > 0)
      return prefix + prefix; 
    return body;
  }
  
  public static List<Descriptor<CrumbIssuer>> getCrumbIssuerDescriptors() { return CrumbIssuer.all(); }
  
  public static String getCrumb(StaplerRequest req) {
    Jenkins h = Jenkins.getInstanceOrNull();
    CrumbIssuer issuer = (h != null) ? h.getCrumbIssuer() : null;
    return (issuer != null) ? issuer.getCrumb(req) : "";
  }
  
  public static String getCrumbRequestField() {
    h = Jenkins.getInstanceOrNull();
    CrumbIssuer issuer = (h != null) ? h.getCrumbIssuer() : null;
    return (issuer != null) ? issuer.getDescriptor().getCrumbRequestField() : "";
  }
  
  public static Date getCurrentTime() { return new Date(); }
  
  public static Locale getCurrentLocale() {
    locale = null;
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req != null)
      locale = req.getLocale(); 
    if (locale == null)
      locale = Locale.getDefault(); 
    return locale;
  }
  
  public static String generateConsoleAnnotationScriptAndStylesheet() {
    cp = Stapler.getCurrentRequest().getContextPath() + Stapler.getCurrentRequest().getContextPath();
    StringBuilder buf = new StringBuilder();
    for (ConsoleAnnotatorFactory f : ConsoleAnnotatorFactory.all()) {
      String path = cp + "/extensionList/" + cp + "/" + ConsoleAnnotatorFactory.class.getName();
      if (f.hasScript())
        buf.append("<script src='").append(path).append("/script.js'></script>"); 
      if (f.hasStylesheet())
        buf.append("<link rel='stylesheet' type='text/css' href='").append(path).append("/style.css' />"); 
    } 
    for (ConsoleAnnotationDescriptor d : ConsoleAnnotationDescriptor.all()) {
      String path = cp + "/descriptor/" + cp;
      if (d.hasScript())
        buf.append("<script src='").append(path).append("/script.js'></script>"); 
      if (d.hasStylesheet())
        buf.append("<link rel='stylesheet' type='text/css' href='").append(path).append("/style.css' />"); 
    } 
    return buf.toString();
  }
  
  public List<String> getLoggerNames() {
    while (true) {
      try {
        List<String> r = new ArrayList<String>();
        Enumeration<String> e = LogManager.getLogManager().getLoggerNames();
        while (e.hasMoreElements())
          r.add((String)e.nextElement()); 
        return r;
      } catch (ConcurrentModificationException concurrentModificationException) {}
    } 
  }
  
  public String getPasswordValue(Object o) {
    if (o == null)
      return null; 
    if (o.equals("<DEFAULT>"))
      return o.toString(); 
    StaplerRequest req = Stapler.getCurrentRequest();
    if ((o instanceof Secret || Secret.BLANK_NONSECRET_PASSWORD_FIELDS_WITHOUT_ITEM_CONFIGURE) && 
      req != null) {
      Item item = (Item)req.findAncestorObject(Item.class);
      if (item != null && !item.hasPermission(Item.CONFIGURE))
        return "********"; 
      Computer computer = (Computer)req.findAncestorObject(Computer.class);
      if (computer != null && !computer.hasPermission(Computer.CONFIGURE))
        return "********"; 
    } 
    if (o instanceof Secret)
      return ((Secret)o).getEncryptedValue(); 
    if (req != null && (Boolean.getBoolean("hudson.hpi.run") || Boolean.getBoolean("hudson.Main.development")))
      LOGGER.log(Level.WARNING, () -> "<f:password/> form control in " + getJellyViewsInformationForCurrentRequest() + " is not backed by hudson.util.Secret. Learn more: https://www.jenkins.io/redirect/hudson.util.Secret"); 
    if (!Secret.AUTO_ENCRYPT_PASSWORD_CONTROL)
      return o.toString(); 
    return Secret.fromString(o.toString()).getEncryptedValue();
  }
  
  private String getJellyViewsInformationForCurrentRequest() {
    Thread thread = Thread.currentThread();
    String threadName = thread.getName();
    String views = (String)Arrays.stream(threadName.split(" ")).filter(part -> {
          int slash = part.lastIndexOf("/");
          int firstPeriod = part.indexOf(".");
          return (slash > 0 && firstPeriod > 0 && slash < firstPeriod);
        }).collect(Collectors.joining(" "));
    if (StringUtils.isBlank(views))
      return threadName; 
    return views;
  }
  
  public List filterDescriptors(Object context, Iterable descriptors) { return DescriptorVisibilityFilter.apply(context, descriptors); }
  
  public static boolean getIsUnitTest() { return Main.isUnitTest; }
  
  public static boolean isArtifactsPermissionEnabled() { return SystemProperties.getBoolean("hudson.security.ArtifactsPermission"); }
  
  public static boolean isWipeOutPermissionEnabled() { return SystemProperties.getBoolean("hudson.security.WipeOutPermission"); }
  
  public static String createRenderOnDemandProxy(JellyContext context, String attributesToCapture) { return Stapler.getCurrentRequest().createJavaScriptProxy(new RenderOnDemandClosure(context, attributesToCapture)); }
  
  public static String getCurrentDescriptorByNameUrl() { return Descriptor.getCurrentDescriptorByNameUrl(); }
  
  public static String setCurrentDescriptorByNameUrl(String value) {
    String o = getCurrentDescriptorByNameUrl();
    Stapler.getCurrentRequest().setAttribute("currentDescriptorByNameUrl", value);
    return o;
  }
  
  public static void restoreCurrentDescriptorByNameUrl(String old) { Stapler.getCurrentRequest().setAttribute("currentDescriptorByNameUrl", old); }
  
  public static List<String> getRequestHeaders(String name) {
    List<String> r = new ArrayList<String>();
    Enumeration e = Stapler.getCurrentRequest().getHeaders(name);
    while (e.hasMoreElements())
      r.add(e.nextElement().toString()); 
    return r;
  }
  
  public static Object rawHtml(Object o) { return (o == null) ? null : new RawHtmlArgument(o); }
  
  public static ArrayList<CLICommand> getCLICommands() {
    all = new ArrayList(CLICommand.all());
    all.sort(Comparator.comparing(CLICommand::getName));
    return all;
  }
  
  public static String getAvatar(User user, String avatarSize) { return UserAvatarResolver.resolve(user, avatarSize); }
  
  @Deprecated
  public String getUserAvatar(User user, String avatarSize) { return getAvatar(user, avatarSize); }
  
  public static String humanReadableByteSize(long size) {
    String measure = "B";
    if (size < 1024L)
      return "" + size + " " + size; 
    double number = size;
    if (number >= 1024.0D) {
      number /= 1024.0D;
      measure = "KB";
      if (number >= 1024.0D) {
        number /= 1024.0D;
        measure = "MB";
        if (number >= 1024.0D) {
          number /= 1024.0D;
          measure = "GB";
        } 
      } 
    } 
    DecimalFormat format = new DecimalFormat("#0.00");
    return format.format(number) + " " + format.format(number);
  }
  
  public static String breakableString(String plain) {
    if (plain == null)
      return null; 
    return plain.replaceAll("([\\p{Punct}&&[^;]]+\\w)", "<wbr>$1")
      .replaceAll("([^\\p{Punct}\\s-]{20})(?=[^\\p{Punct}\\s-]{10})", "$1<wbr>");
  }
  
  public static void advertiseHeaders(HttpServletResponse rsp) {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null) {
      rsp.setHeader("X-Hudson", "1.395");
      rsp.setHeader("X-Jenkins", Jenkins.VERSION);
      rsp.setHeader("X-Jenkins-Session", Jenkins.SESSION_HASH);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isContextMenuVisible(Action a) {
    if (a instanceof ModelObjectWithContextMenu.ContextMenuVisibility)
      return ((ModelObjectWithContextMenu.ContextMenuVisibility)a).isVisible(); 
    return true;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Icon tryGetIcon(String iconGuess) {
    if (iconGuess == null || iconGuess.startsWith("symbol-"))
      return null; 
    Icon iconMetadata = IconSet.icons.getIconByClassSpec(iconGuess);
    if (iconMetadata == null && iconGuess.contains(" "))
      iconMetadata = IconSet.icons.getIconByClassSpec(filterIconNameClasses(iconGuess)); 
    if (iconMetadata == null)
      iconMetadata = IconSet.icons.getIconByClassSpec(IconSet.toNormalizedIconNameClass(iconGuess) + " icon-md"); 
    if (iconMetadata == null)
      iconMetadata = IconSet.icons.getIconByUrl(iconGuess); 
    return iconMetadata;
  }
  
  @NonNull
  private static String filterIconNameClasses(@NonNull String classNames) {
    return (String)Arrays.stream(StringUtils.split(classNames, ' '))
      .filter(className -> className.startsWith("icon-"))
      .collect(Collectors.joining(" "));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String extractPluginNameFromIconSrc(String iconSrc) {
    if (iconSrc == null)
      return ""; 
    if (!iconSrc.contains("plugin-"))
      return ""; 
    String[] arr = iconSrc.split(" ");
    for (String element : arr) {
      if (element.startsWith("plugin-"))
        return element.replaceFirst("plugin-", ""); 
    } 
    return "";
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String tryGetIconPath(String iconGuess, JellyContext context) {
    String iconSource;
    if (iconGuess == null)
      return null; 
    if (iconGuess.startsWith("symbol-"))
      return iconGuess; 
    StaplerRequest currentRequest = Stapler.getCurrentRequest();
    String rootURL = currentRequest.getContextPath();
    Icon iconMetadata = tryGetIcon(iconGuess);
    if (iconMetadata != null) {
      iconSource = IconSet.tryTranslateTangoIconToSymbol(iconMetadata.getClassSpec(), () -> iconMetadata.getQualifiedUrl(context));
    } else {
      iconSource = guessIcon(iconGuess, rootURL);
    } 
    return iconSource;
  }
  
  static String guessIcon(String iconGuess, String rootURL) {
    String iconSource;
    if (iconGuess.startsWith("http://") || iconGuess.startsWith("https://")) {
      iconSource = iconGuess;
    } else {
      if (!iconGuess.startsWith("/"))
        iconGuess = "/" + iconGuess; 
      if (iconGuess.startsWith(rootURL) && ((
        !rootURL.equals("/images") && !rootURL.equals("/plugin")) || iconGuess.startsWith(rootURL + rootURL)))
        iconGuess = iconGuess.substring(rootURL.length()); 
      iconSource = rootURL + rootURL + ((iconGuess.startsWith("/images/") || iconGuess.startsWith("/plugin/")) ? getResourcePath() : "");
    } 
    return iconSource;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"PREDICTABLE_RANDOM"}, justification = "True randomness isn't necessary for form item IDs")
  public static String generateItemId() { return String.valueOf(Math.floor(Math.random() * 3000.0D)); }
}
