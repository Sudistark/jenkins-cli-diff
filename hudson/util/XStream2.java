package hudson.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.core.util.Fields;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.thoughtworks.xstream.security.AnyTypePermission;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.diagnosis.OldDataMonitor;
import hudson.model.Label;
import hudson.model.Saveable;
import hudson.util.xstream.ImmutableListConverter;
import hudson.util.xstream.ImmutableMapConverter;
import hudson.util.xstream.ImmutableSetConverter;
import hudson.util.xstream.ImmutableSortedSetConverter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.xstream.SafeURLConverter;
import org.kohsuke.accmod.Restricted;

public class XStream2 extends XStream {
  private static final Logger LOGGER = Logger.getLogger(XStream2.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String COLLECTION_UPDATE_LIMIT_PROPERTY_NAME = XStream2.class.getName() + ".collectionUpdateLimit";
  
  private static final int COLLECTION_UPDATE_LIMIT_DEFAULT_VALUE = 5;
  
  private RobustReflectionConverter reflectionConverter;
  
  private final ThreadLocal<Boolean> oldData = new ThreadLocal();
  
  @CheckForNull
  private final ClassOwnership classOwnership;
  
  private final Map<String, Class<?>> compatibilityAliases = new ConcurrentHashMap();
  
  private MapperInjectionPoint mapperInjectionPoint;
  
  public static HierarchicalStreamDriver getDefaultDriver() { return new StaxDriver(); }
  
  public XStream2() {
    super(getDefaultDriver());
    init();
    this.classOwnership = null;
  }
  
  public XStream2(HierarchicalStreamDriver hierarchicalStreamDriver) {
    super(hierarchicalStreamDriver);
    init();
    this.classOwnership = null;
  }
  
  public XStream2(ReflectionProvider reflectionProvider, HierarchicalStreamDriver driver, ClassLoaderReference classLoaderReference, Mapper mapper, ConverterLookup converterLookup, ConverterRegistry converterRegistry) {
    super(reflectionProvider, driver, classLoaderReference, mapper, converterLookup, converterRegistry);
    init();
    this.classOwnership = null;
  }
  
  XStream2(ClassOwnership classOwnership) {
    super(getDefaultDriver());
    init();
    this.classOwnership = classOwnership;
  }
  
  public Object unmarshal(HierarchicalStreamReader reader, Object root, DataHolder dataHolder) { return unmarshal(reader, root, dataHolder, false); }
  
  public Object unmarshal(HierarchicalStreamReader reader, Object root, DataHolder dataHolder, boolean nullOut) {
    Object o;
    Jenkins h = Jenkins.getInstanceOrNull();
    if (h != null && h.pluginManager != null && h.pluginManager.uberClassLoader != null)
      setClassLoader(h.pluginManager.uberClassLoader); 
    if (root == null || !nullOut) {
      o = super.unmarshal(reader, root, dataHolder);
    } else {
      Set<String> topLevelFields = new HashSet<String>();
      o = super.unmarshal(new Object(this, reader, topLevelFields), root, dataHolder);
      if (o == root && getConverterLookup().lookupConverterForType(o.getClass()) instanceof RobustReflectionConverter)
        getReflectionProvider().visitSerializableFields(o, (name, type, definedIn, value) -> {
              Object v;
              if (topLevelFields.contains(name))
                return; 
              Field f = Fields.find(definedIn, name);
              if (type.isPrimitive()) {
                v = ReflectionUtils.getVmDefaultValueForPrimitiveType(type);
                if (v.equals(value))
                  return; 
              } else {
                if (value == null)
                  return; 
                v = null;
              } 
              LOGGER.log(Level.FINE, "JENKINS-21017: nulling out {0} in {1}", new Object[] { f, o });
              Fields.write(f, o, v);
            }); 
    } 
    if (this.oldData.get() != null) {
      this.oldData.remove();
      if (o instanceof Saveable)
        OldDataMonitor.report((Saveable)o, "1.106"); 
    } 
    return o;
  }
  
  protected void setupConverters() {
    super.setupConverters();
    this.reflectionConverter = new RobustReflectionConverter(getMapper(), JVM.newReflectionProvider(), new PluginClassOwnership(this));
    registerConverter(this.reflectionConverter, -19);
  }
  
  public void addCriticalField(Class<?> clazz, String field) { this.reflectionConverter.addCriticalField(clazz, field); }
  
  static String trimVersion(String version) { return version.replaceFirst(" .+$", ""); }
  
  private void init() {
    int updateLimit = SystemProperties.getInteger(COLLECTION_UPDATE_LIMIT_PROPERTY_NAME, Integer.valueOf(5)).intValue();
    setCollectionUpdateLimit(updateLimit);
    addImmutableType(hudson.model.Result.class, false);
    denyTypes(new Class[] { void.class, Void.class });
    registerConverter(new RobustCollectionConverter(getMapper(), getReflectionProvider()), 10);
    registerConverter(new RobustMapConverter(getMapper()), 10);
    registerConverter(new ImmutableMapConverter(getMapper(), getReflectionProvider()), 10);
    registerConverter(new ImmutableSortedSetConverter(getMapper(), getReflectionProvider()), 10);
    registerConverter(new ImmutableSetConverter(getMapper(), getReflectionProvider()), 10);
    registerConverter(new ImmutableListConverter(getMapper(), getReflectionProvider()), 10);
    registerConverter(new CopyOnWriteMap.Tree.ConverterImpl(getMapper()), 10);
    registerConverter(new DescribableList.ConverterImpl(getMapper()), 10);
    registerConverter(new Label.ConverterImpl(), 10);
    registerConverter(new SafeURLConverter(), 10);
    registerConverter(new AssociatedConverterImpl(this), -10);
    registerConverter(new BlacklistedTypesConverter(), 10000);
    addPermission(AnyTypePermission.ANY);
    registerConverter(new Object(this, getMapper(), new ClassLoaderReference(getClassLoader())), 10000);
  }
  
  protected MapperWrapper wrapMapper(MapperWrapper next) {
    CompatibilityMapper compatibilityMapper = new CompatibilityMapper(this, new Object(this, next));
    this.mapperInjectionPoint = new MapperInjectionPoint(compatibilityMapper);
    return this.mapperInjectionPoint;
  }
  
  public Mapper getMapperInjectionPoint() { return this.mapperInjectionPoint.getDelegate(); }
  
  @Deprecated
  public void toXML(Object obj, OutputStream out) { super.toXML(obj, out); }
  
  public void toXMLUTF8(Object obj, OutputStream out) {
    Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    w.write("<?xml version=\"1.1\" encoding=\"UTF-8\"?>\n");
    toXML(obj, w);
  }
  
  public void setMapper(Mapper m) { this.mapperInjectionPoint.setDelegate(m); }
  
  public void addCompatibilityAlias(String oldClassName, Class newClass) { this.compatibilityAliases.put(oldClassName, newClass); }
}
