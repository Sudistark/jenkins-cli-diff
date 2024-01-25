package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import hudson.RelativePath;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.listeners.SaveableListener;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ReflectionUtils;
import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import jenkins.security.RedactSecretJsonInErrorMessageSanitizer;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.BindInterceptor;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.lang.Klass;

public abstract class Descriptor<T extends Describable<T>> extends Object implements Saveable, OnMaster {
  public final Class<? extends T> clazz;
  
  private final Map<String, FormValidation.CheckMethod> checkMethods = new ConcurrentHashMap(2);
  
  private final Map<String, HelpRedirect> helpRedirect = new HashMap(2);
  
  protected Descriptor(Class<? extends T> clazz) {
    if (clazz == self())
      clazz = getClass(); 
    this.clazz = clazz;
  }
  
  protected Descriptor() {
    this.clazz = getClass().getEnclosingClass();
    if (this.clazz == null)
      throw new AssertionError("" + getClass() + " doesn't have an outer class. Use the constructor that takes the Class object explicitly."); 
    Type bt = Types.getBaseClass(getClass(), Descriptor.class);
    if (bt instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType)bt;
      Class t = Types.erasure(pt.getActualTypeArguments()[0]);
      if (!t.isAssignableFrom(this.clazz))
        throw new AssertionError("Outer class " + this.clazz + " of " + getClass() + " is not assignable to " + t + ". Perhaps wrong outer class?"); 
    } 
    try {
      Method getd = this.clazz.getMethod("getDescriptor", new Class[0]);
      if (!getd.getReturnType().isAssignableFrom(getClass()))
        throw new AssertionError("" + getClass() + " must be assignable to " + getClass()); 
    } catch (NoSuchMethodException e) {
      throw new AssertionError("" + getClass() + " is missing getDescriptor method.", e);
    } 
  }
  
  @NonNull
  public String getDisplayName() { return this.clazz.getSimpleName(); }
  
  public String getId() { return this.clazz.getName(); }
  
  public Class<T> getT() {
    Type subTyping = Types.getBaseClass(getClass(), Descriptor.class);
    if (!(subTyping instanceof ParameterizedType))
      throw new IllegalStateException("" + getClass() + " doesn't extend Descriptor with a type parameter."); 
    return Types.erasure(Types.getTypeArgument(subTyping, 0));
  }
  
  public String getDescriptorUrl() { return "descriptorByName/" + getId(); }
  
  public final String getDescriptorFullUrl() { return getCurrentDescriptorByNameUrl() + "/" + getCurrentDescriptorByNameUrl(); }
  
  public static String getCurrentDescriptorByNameUrl() {
    req = Stapler.getCurrentRequest();
    Object url = req.getAttribute("currentDescriptorByNameUrl");
    if (url != null)
      return url.toString(); 
    Ancestor a = req.findAncestor(DescriptorByNameOwner.class);
    return a.getUrl();
  }
  
  @Deprecated
  public String getCheckUrl(String fieldName) { return getCheckMethod(fieldName).toCheckUrl(); }
  
  public FormValidation.CheckMethod getCheckMethod(String fieldName) {
    FormValidation.CheckMethod method = (FormValidation.CheckMethod)this.checkMethods.get(fieldName);
    if (method == null) {
      method = new FormValidation.CheckMethod(this, fieldName);
      this.checkMethods.put(fieldName, method);
    } 
    return method;
  }
  
  public void calcFillSettings(String field, Map<String, Object> attributes) {
    String capitalizedFieldName = StringUtils.capitalize(field);
    String methodName = "doFill" + capitalizedFieldName + "Items";
    Method method = ReflectionUtils.getPublicMethodNamed(getClass(), methodName);
    if (method == null)
      throw new IllegalStateException(String.format("%s doesn't have the %s method for filling a drop-down list", new Object[] { getClass(), methodName })); 
    List<String> depends = buildFillDependencies(method, new ArrayList());
    if (!depends.isEmpty())
      attributes.put("fillDependsOn", String.join(" ", depends)); 
    attributes.put("fillUrl", String.format("%s/%s/fill%sItems", new Object[] { getCurrentDescriptorByNameUrl(), getDescriptorUrl(), capitalizedFieldName }));
  }
  
  private List<String> buildFillDependencies(Method method, List<String> depends) {
    for (ReflectionUtils.Parameter p : ReflectionUtils.getParameters(method)) {
      QueryParameter qp = (QueryParameter)p.annotation(QueryParameter.class);
      if (qp != null) {
        String name = qp.value();
        if (name.isEmpty())
          name = p.name(); 
        if (name == null || name.isEmpty())
          continue; 
        RelativePath rp = (RelativePath)p.annotation(RelativePath.class);
        if (rp != null)
          name = rp.value() + "/" + rp.value(); 
        depends.add(name);
        continue;
      } 
      Method m = ReflectionUtils.getPublicMethodNamed(p.type(), "fromStapler");
      if (m != null)
        buildFillDependencies(m, depends); 
    } 
    return depends;
  }
  
  public void calcAutoCompleteSettings(String field, Map<String, Object> attributes) {
    String capitalizedFieldName = StringUtils.capitalize(field);
    String methodName = "doAutoComplete" + capitalizedFieldName;
    Method method = ReflectionUtils.getPublicMethodNamed(getClass(), methodName);
    if (method == null)
      return; 
    attributes.put("autoCompleteUrl", String.format("%s/%s/autoComplete%s", new Object[] { getCurrentDescriptorByNameUrl(), getDescriptorUrl(), capitalizedFieldName }));
  }
  
  @CheckForNull
  public PropertyType getPropertyType(@NonNull Object instance, @NonNull String field) { return (instance == this) ? getGlobalPropertyType(field) : getPropertyType(field); }
  
  @NonNull
  public PropertyType getPropertyTypeOrDie(@NonNull Object instance, @NonNull String field) {
    PropertyType propertyType = getPropertyType(instance, field);
    if (propertyType != null)
      return propertyType; 
    if (instance == this)
      throw new AssertionError(getClass().getName() + " has no property " + getClass().getName()); 
    throw new AssertionError(this.clazz.getName() + " has no property " + this.clazz.getName());
  }
  
  public PropertyType getPropertyType(String field) {
    if (this.propertyTypes == null)
      this.propertyTypes = buildPropertyTypes(this.clazz); 
    return (PropertyType)this.propertyTypes.get(field);
  }
  
  public PropertyType getGlobalPropertyType(String field) {
    if (this.globalPropertyTypes == null)
      this.globalPropertyTypes = buildPropertyTypes(getClass()); 
    return (PropertyType)this.globalPropertyTypes.get(field);
  }
  
  private Map<String, PropertyType> buildPropertyTypes(Class<?> clazz) {
    Map<String, PropertyType> r = new HashMap<String, PropertyType>();
    for (Field f : clazz.getFields())
      r.put(f.getName(), new PropertyType(f)); 
    for (Method m : clazz.getMethods()) {
      if (m.getName().startsWith("get"))
        r.put(Introspector.decapitalize(m.getName().substring(3)), new PropertyType(m)); 
    } 
    return r;
  }
  
  public final String getJsonSafeClassName() { return getId().replace('.', '-'); }
  
  @Deprecated
  public T newInstance(StaplerRequest req) throws FormException {
    throw new UnsupportedOperationException("" + getClass() + " should implement newInstance(StaplerRequest,JSONObject)");
  }
  
  public T newInstance(@Nullable StaplerRequest req, @NonNull JSONObject formData) throws FormException {
    try {
      Method m = getClass().getMethod("newInstance", new Class[] { StaplerRequest.class });
      if (!Modifier.isAbstract(m.getDeclaringClass().getModifiers()))
        return (T)verifyNewInstance(newInstance(req)); 
      if (req == null)
        return (T)verifyNewInstance((Describable)this.clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0])); 
      return (T)verifyNewInstance((Describable)bindJSON(req, this.clazz, formData, true));
    } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|java.lang.reflect.InvocationTargetException|RuntimeException e) {
      throw new LinkageError("Failed to instantiate " + this.clazz + " from " + RedactSecretJsonInErrorMessageSanitizer.INSTANCE.sanitize(formData), e);
    } 
  }
  
  public static <T> T bindJSON(StaplerRequest req, Class<T> type, JSONObject src) { return (T)bindJSON(req, type, src, false); }
  
  private static <T> T bindJSON(StaplerRequest req, Class<T> type, JSONObject src, boolean fromNewInstance) {
    oldInterceptor = req.getBindInterceptor();
    try {
      NewInstanceBindInterceptor interceptor;
      if (oldInterceptor instanceof NewInstanceBindInterceptor) {
        interceptor = (NewInstanceBindInterceptor)oldInterceptor;
      } else {
        interceptor = new NewInstanceBindInterceptor(oldInterceptor);
        req.setBindInterceptor(interceptor);
      } 
      if (fromNewInstance)
        interceptor.processed.put(src, Boolean.valueOf(true)); 
      object = req.bindJSON(type, src);
      return (T)object;
    } finally {
      req.setBindInterceptor(oldInterceptor);
    } 
  }
  
  private T verifyNewInstance(T t) {
    if (t != null && t.getDescriptor() != this)
      LOGGER.warning("Father of " + t + " and its getDescriptor() points to two different instances. Probably misplaced @Extension. See http://hudson.361315.n4.nabble.com/Help-Hint-needed-Post-build-action-doesn-t-stay-activated-td2308833.html"); 
    return t;
  }
  
  public Klass<?> getKlass() { return Klass.java(this.clazz); }
  
  public String getHelpFile() { return getHelpFile(null); }
  
  public String getHelpFile(String fieldName) { return getHelpFile(getKlass(), fieldName); }
  
  @SuppressFBWarnings(value = {"SBSC_USE_STRINGBUFFER_CONCATENATION"}, justification = "no big deal")
  public String getHelpFile(Klass<?> clazz, String fieldName) {
    HelpRedirect r = (HelpRedirect)this.helpRedirect.get(fieldName);
    if (r != null)
      return r.resolve(); 
    for (Klass<?> c : clazz.getAncestors()) {
      String suffix, page = "/descriptor/" + getId() + "/help";
      if (fieldName == null) {
        suffix = "";
      } else {
        page = page + "/" + page;
        suffix = "-" + fieldName;
      } 
      try {
        if (Stapler.getCurrentRequest().getView(c, "help" + suffix) != null)
          return page; 
      } catch (IOException e) {
        throw new Error(e);
      } 
      if (getStaticHelpUrl(c, suffix) != null)
        return page; 
    } 
    return null;
  }
  
  protected void addHelpFileRedirect(String fieldName, Class<? extends Describable> owner, String fieldNameToRedirectTo) { this.helpRedirect.put(fieldName, new HelpRedirect(owner, fieldNameToRedirectTo)); }
  
  public final boolean isInstance(T instance) { return this.clazz.isInstance(instance); }
  
  public final boolean isSubTypeOf(Class type) { return type.isAssignableFrom(this.clazz); }
  
  @Deprecated
  public boolean configure(StaplerRequest req) throws FormException { return true; }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws FormException { return configure(req); }
  
  public String getConfigPage() { return getViewPage(this.clazz, getPossibleViewNames("config"), "config.jelly"); }
  
  public String getGlobalConfigPage() { return getViewPage(this.clazz, getPossibleViewNames("global"), null); }
  
  @NonNull
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Unclassified.class); }
  
  @NonNull
  public Permission getRequiredGlobalConfigPagePermission() { return Jenkins.ADMINISTER; }
  
  private String getViewPage(Class<?> clazz, String pageName, String defaultValue) { return getViewPage(clazz, Set.of(pageName), defaultValue); }
  
  private String getViewPage(Class<?> clazz, Collection<String> pageNames, String defaultValue) {
    while (clazz != Object.class && clazz != null) {
      for (String pageName : pageNames) {
        String name = clazz.getName().replace('.', '/').replace('$', '/') + "/" + clazz.getName().replace('.', '/').replace('$', '/');
        if (clazz.getClassLoader().getResource(name) != null)
          return "/" + name; 
      } 
      clazz = clazz.getSuperclass();
    } 
    return defaultValue;
  }
  
  protected final String getViewPage(Class<?> clazz, String pageName) { return getViewPage(clazz, pageName, pageName); }
  
  protected List<String> getPossibleViewNames(String baseName) {
    List<String> names = new ArrayList<String>();
    for (Facet f : (WebApp.get((Jenkins.get()).servletContext)).facets) {
      if (f instanceof JellyCompatibleFacet) {
        JellyCompatibleFacet jcf = (JellyCompatibleFacet)f;
        for (String ext : jcf.getScriptExtensions())
          names.add(baseName + baseName); 
      } 
    } 
    return names;
  }
  
  public void save() {
    if (BulkChange.contains(this))
      return; 
    try {
      getConfigFile().write(this);
      SaveableListener.fireOnChange(this, getConfigFile());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to save " + getConfigFile(), e);
    } 
  }
  
  public void load() {
    XmlFile file = getConfigFile();
    if (!file.exists())
      return; 
    try {
      file.unmarshal(this);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load " + file, e);
    } 
  }
  
  protected XmlFile getConfigFile() {
    return new XmlFile(new File(Jenkins.get().getRootDir(), getId() + ".xml"));
  }
  
  protected PluginWrapper getPlugin() { return Jenkins.get().getPluginManager().whichPlugin(this.clazz); }
  
  public void doHelp(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    String path = req.getRestOfPath();
    if (path.contains(".."))
      throw new ServletException("Illegal path: " + path); 
    path = path.replace('/', '-');
    PluginWrapper pw = getPlugin();
    if (pw != null) {
      rsp.setHeader("X-Plugin-Short-Name", pw.getShortName());
      rsp.setHeader("X-Plugin-Long-Name", pw.getLongName());
      rsp.setHeader("X-Plugin-From", Messages.Descriptor_From(pw
            .getLongName().replace("Hudson", "Jenkins").replace("hudson", "jenkins"), pw.getUrl()));
    } 
    for (Klass<?> c = getKlass(); c != null; c = c.getSuperClass()) {
      RequestDispatcher rd = Stapler.getCurrentRequest().getView(c, "help" + path);
      if (rd != null) {
        rd.forward(req, rsp);
        return;
      } 
      URL url = getStaticHelpUrl(c, path);
      if (url != null) {
        rsp.setContentType("text/html;charset=UTF-8");
        InputStream in = url.openStream();
        try {
          String literal = IOUtils.toString(in, StandardCharsets.UTF_8);
          rsp.getWriter().println(Util.replaceMacro(literal, Map.of("rootURL", req.getContextPath())));
          if (in != null)
            in.close(); 
        } catch (Throwable throwable) {
          if (in != null)
            try {
              in.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        return;
      } 
    } 
    rsp.sendError(404);
  }
  
  private URL getStaticHelpUrl(Klass<?> c, String suffix) {
    Locale locale = Stapler.getCurrentRequest().getLocale();
    String base = "help" + suffix;
    URL url = c.getResource(base + "_" + base + "_" + locale.getLanguage() + "_" + locale.getCountry() + ".html");
    if (url != null)
      return url; 
    url = c.getResource(base + "_" + base + "_" + locale.getLanguage() + ".html");
    if (url != null)
      return url; 
    url = c.getResource(base + "_" + base + ".html");
    if (url != null)
      return url; 
    return c.getResource(base + ".html");
  }
  
  public static <T> T[] toArray(T... values) { return values; }
  
  public static <T> List<T> toList(T... values) { return new ArrayList(Arrays.asList(values)); }
  
  public static <T extends Describable<T>> Map<Descriptor<T>, T> toMap(Iterable<T> describables) {
    Map<Descriptor<T>, T> m = new LinkedHashMap<Descriptor<T>, T>();
    for (Iterator iterator = describables.iterator(); iterator.hasNext(); ) {
      Descriptor<T> descriptor;
      T d = (T)(Describable)iterator.next();
      try {
        descriptor = d.getDescriptor();
      } catch (Throwable x) {
        LOGGER.log(Level.WARNING, null, x);
        continue;
      } 
      m.put(descriptor, d);
    } 
    return m;
  }
  
  public static <T extends Describable<T>> List<T> newInstancesFromHeteroList(StaplerRequest req, JSONObject formData, String key, Collection<? extends Descriptor<T>> descriptors) throws FormException { return newInstancesFromHeteroList(req, formData.get(key), descriptors); }
  
  public static <T extends Describable<T>> List<T> newInstancesFromHeteroList(StaplerRequest req, Object formData, Collection<? extends Descriptor<T>> descriptors) throws FormException {
    List<T> items = new ArrayList<T>();
    if (formData != null)
      for (Object o : JSONArray.fromObject(formData)) {
        JSONObject jo = (JSONObject)o;
        Descriptor<T> d = null;
        String kind = jo.optString("kind", null);
        if (kind != null)
          d = findById(descriptors, kind); 
        if (d == null) {
          kind = jo.optString("$class");
          if (kind != null) {
            d = findByDescribableClassName(descriptors, kind);
            if (d == null)
              d = findByClassName(descriptors, kind); 
          } 
        } 
        if (d != null) {
          items.add(d.newInstance(req, jo));
          continue;
        } 
        LOGGER.log(Level.WARNING, "Received unexpected form data element: {0}", jo);
      }  
    return items;
  }
  
  @CheckForNull
  public static <T extends Descriptor> T findById(Collection<? extends T> list, String id) {
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      T d = (T)(Descriptor)iterator.next();
      if (d.getId().equals(id))
        return d; 
    } 
    return null;
  }
  
  @CheckForNull
  private static <T extends Descriptor> T findByClassName(Collection<? extends T> list, String className) {
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      T d = (T)(Descriptor)iterator.next();
      if (d.getClass().getName().equals(className))
        return d; 
    } 
    return null;
  }
  
  @CheckForNull
  public static <T extends Descriptor> T findByDescribableClassName(Collection<? extends T> list, String className) {
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      T d = (T)(Descriptor)iterator.next();
      if (d.clazz.getName().equals(className))
        return d; 
    } 
    return null;
  }
  
  @Deprecated
  @CheckForNull
  public static <T extends Descriptor> T find(Collection<? extends T> list, String string) {
    T d = (T)findByClassName(list, string);
    if (d != null)
      return d; 
    return (T)findById(list, string);
  }
  
  @Deprecated
  @CheckForNull
  public static Descriptor find(String className) { return find(ExtensionList.lookup(Descriptor.class), className); }
  
  private static final Logger LOGGER = Logger.getLogger(Descriptor.class.getName());
  
  protected static Class self() { return Self.class; }
}
