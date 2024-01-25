package hudson.util;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.core.util.SerializationMembers;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.security.InputManipulationException;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.diagnosis.OldDataMonitor;
import hudson.model.Saveable;
import hudson.security.ACL;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.xstream.CriticalXStreamException;
import net.jcip.annotations.GuardedBy;
import org.acegisecurity.Authentication;

public class RobustReflectionConverter implements Converter {
  private static boolean RECORD_FAILURES_FOR_ALL_AUTHENTICATIONS = SystemProperties.getBoolean(RobustReflectionConverter.class.getName() + ".recordFailuresForAllAuthentications", false);
  
  private static boolean RECORD_FAILURES_FOR_ADMINS = SystemProperties.getBoolean(RobustReflectionConverter.class.getName() + ".recordFailuresForAdmins", false);
  
  protected final ReflectionProvider reflectionProvider;
  
  protected final Mapper mapper;
  
  protected SerializationMembers serializationMethodInvoker;
  
  private ReflectionProvider pureJavaReflectionProvider;
  
  @NonNull
  private final XStream2.ClassOwnership classOwnership;
  
  private final ReadWriteLock criticalFieldsLock;
  
  @GuardedBy("criticalFieldsLock")
  private final Map<String, Set<String>> criticalFields;
  
  public RobustReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) { this(mapper, reflectionProvider, new XStream2.PluginClassOwnership(new XStream2())); }
  
  RobustReflectionConverter(Mapper mapper, ReflectionProvider reflectionProvider, XStream2.ClassOwnership classOwnership) {
    this.criticalFieldsLock = new ReentrantReadWriteLock();
    this.criticalFields = new HashMap();
    this.mapper = mapper;
    this.reflectionProvider = reflectionProvider;
    assert classOwnership != null;
    this.classOwnership = classOwnership;
    this.serializationMethodInvoker = new SerializationMembers();
  }
  
  void addCriticalField(Class<?> clazz, String field) {
    this.criticalFieldsLock.writeLock().lock();
    try {
      if (!this.criticalFields.containsKey(field))
        this.criticalFields.put(field, new HashSet()); 
      ((Set)this.criticalFields.get(field)).add(clazz.getName());
    } finally {
      this.criticalFieldsLock.writeLock().unlock();
    } 
  }
  
  private boolean hasCriticalField(Class<?> clazz, String field) {
    this.criticalFieldsLock.readLock().lock();
    try {
      Set<String> classesWithField = (Set)this.criticalFields.get(field);
      if (classesWithField == null)
        return false; 
      if (!classesWithField.contains(clazz.getName()))
        return false; 
      return true;
    } finally {
      this.criticalFieldsLock.readLock().unlock();
    } 
  }
  
  public boolean canConvert(Class type) { return true; }
  
  public void marshal(Object original, HierarchicalStreamWriter writer, MarshallingContext context) {
    Object source = this.serializationMethodInvoker.callWriteReplace(original);
    if (source.getClass() != original.getClass())
      writer.addAttribute(this.mapper.aliasForAttribute("resolves-to"), this.mapper.serializedClass(source.getClass())); 
    oc = OwnerContext.find(context);
    oc.startVisiting(writer, this.classOwnership.ownerOf(original.getClass()));
    try {
      doMarshal(source, writer, context);
    } finally {
      oc.stopVisiting();
    } 
  }
  
  protected void doMarshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Set seenFields = new HashSet();
    Set seenAsAttributes = new HashSet();
    this.reflectionProvider.visitSerializableFields(source, new Object(this, writer, seenAsAttributes));
    this.reflectionProvider.visitSerializableFields(source, new Object(this, seenAsAttributes, source, context, seenFields, writer));
  }
  
  protected void marshallField(MarshallingContext context, Object newObj, Field field) {
    Converter converter = this.mapper.getLocalConverter(field.getDeclaringClass(), field.getName());
    context.convertAnother(newObj, converter);
  }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Object result = instantiateNewInstance(reader, context);
    result = doUnmarshal(result, reader, context);
    return this.serializationMethodInvoker.callReadResolve(result);
  }
  
  public Object doUnmarshal(Object result, HierarchicalStreamReader reader, UnmarshallingContext context) {
    SeenFields seenFields = new SeenFields();
    Iterator it = reader.getAttributeNames();
    if (result instanceof Saveable && context.get("Saveable") == null)
      context.put("Saveable", result); 
    while (it.hasNext()) {
      String attrAlias = (String)it.next();
      String attrName = this.mapper.attributeForAlias(attrAlias);
      Class classDefiningField = determineWhichClassDefinesField(reader);
      boolean fieldExistsInClass = fieldDefinedInClass(result, attrName);
      if (fieldExistsInClass) {
        Field field = this.reflectionProvider.getField(result.getClass(), attrName);
        SingleValueConverter converter = this.mapper.getConverterFromAttribute(field.getDeclaringClass(), attrName, field.getType());
        Class type = field.getType();
        if (converter == null)
          converter = this.mapper.getConverterFromItemType(type); 
        if (converter != null) {
          Object value = converter.fromString(reader.getAttribute(attrAlias));
          if (type.isPrimitive())
            type = Primitives.box(type); 
          if (value != null && !type.isAssignableFrom(value.getClass()))
            throw new ConversionException("Cannot convert type " + value.getClass().getName() + " to type " + type.getName()); 
          this.reflectionProvider.writeField(result, attrName, value, classDefiningField);
          seenFields.add(classDefiningField, attrName);
        } 
      } 
    } 
    Map implicitCollectionsForCurrentObject = null;
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      boolean critical = false;
      try {
        Object value;
        String fieldName = this.mapper.realMember(result.getClass(), reader.getNodeName());
        for (Class<?> concrete = result.getClass(); concrete != null; concrete = concrete.getSuperclass()) {
          if (hasCriticalField(concrete, fieldName)) {
            critical = true;
            break;
          } 
        } 
        boolean implicitCollectionHasSameName = (this.mapper.getImplicitCollectionDefForFieldName(result.getClass(), reader.getNodeName()) != null);
        Class classDefiningField = determineWhichClassDefinesField(reader);
        boolean fieldExistsInClass = (!implicitCollectionHasSameName && fieldDefinedInClass(result, fieldName));
        Class type = determineType(reader, fieldExistsInClass, result, fieldName, classDefiningField);
        if (fieldExistsInClass) {
          Field field = this.reflectionProvider.getField(result.getClass(), fieldName);
          value = unmarshalField(context, result, type, field);
          Class definedType = this.reflectionProvider.getFieldType(result, fieldName, classDefiningField);
          if (!definedType.isPrimitive())
            type = definedType; 
        } else {
          value = context.convertAnother(result, type);
        } 
        if (value != null && !type.isAssignableFrom(value.getClass())) {
          LOGGER.warning("Cannot convert type " + value.getClass().getName() + " to type " + type.getName());
        } else if (fieldExistsInClass) {
          this.reflectionProvider.writeField(result, fieldName, value, classDefiningField);
          seenFields.add(classDefiningField, fieldName);
        } else {
          implicitCollectionsForCurrentObject = writeValueToImplicitCollection(context, value, implicitCollectionsForCurrentObject, result, fieldName);
        } 
      } catch (CriticalXStreamException e) {
        throw e;
      } catch (InputManipulationException e) {
        LOGGER.warning("DoS detected and prevented. If the heuristic was too aggressive, you can customize the behavior by setting the hudson.util.XStream2.collectionUpdateLimit system property. See https://www.jenkins.io/redirect/xstream-dos-prevention for more information.");
        throw new CriticalXStreamException(e);
      } catch (XStreamException e) {
        if (critical)
          throw new CriticalXStreamException(e); 
        addErrorInContext(context, e);
      } catch (LinkageError e) {
        if (critical)
          throw e; 
        addErrorInContext(context, e);
      } 
      reader.moveUp();
    } 
    if (shouldReportUnloadableDataForCurrentUser() && context.get("ReadError") != null && context.get("Saveable") == result) {
      try {
        OldDataMonitor.report((Saveable)result, (ArrayList)context.get("ReadError"));
      } catch (Throwable t) {
        StringBuilder message = new StringBuilder("There was a problem reporting unmarshalling field errors");
        Level level = Level.WARNING;
        if (t instanceof IllegalStateException && t.getMessage().contains("Expected 1 instance of " + OldDataMonitor.class.getName())) {
          message.append(". Make sure this code is executed after InitMilestone.EXTENSIONS_AUGMENTED stage, for example in Plugin#postInitialize instead of Plugin#start");
          level = Level.INFO;
        } 
        LOGGER.log(level, message.toString(), t);
      } 
      context.put("ReadError", null);
    } 
    return result;
  }
  
  private static boolean shouldReportUnloadableDataForCurrentUser() {
    if (RECORD_FAILURES_FOR_ALL_AUTHENTICATIONS)
      return true; 
    authentication = Jenkins.getAuthentication();
    if (authentication.equals(ACL.SYSTEM))
      return true; 
    return (RECORD_FAILURES_FOR_ADMINS && Jenkins.get().hasPermission(Jenkins.ADMINISTER));
  }
  
  public static void addErrorInContext(UnmarshallingContext context, Throwable e) {
    LOGGER.log(Level.FINE, "Failed to load", e);
    ArrayList<Throwable> list = (ArrayList)context.get("ReadError");
    if (list == null)
      context.put("ReadError", list = new ArrayList<Throwable>()); 
    list.add(e);
  }
  
  private boolean fieldDefinedInClass(Object result, String attrName) { return (this.reflectionProvider.getFieldOrNull(result.getClass(), attrName) != null); }
  
  protected Object unmarshalField(UnmarshallingContext context, Object result, Class type, Field field) {
    Converter converter = this.mapper.getLocalConverter(field.getDeclaringClass(), field.getName());
    return context.convertAnother(result, type, converter);
  }
  
  private Map writeValueToImplicitCollection(UnmarshallingContext context, Object value, Map implicitCollections, Object result, String itemFieldName) {
    String fieldName = this.mapper.getFieldNameForItemTypeAndName(context.getRequiredType(), value.getClass(), itemFieldName);
    if (fieldName != null) {
      if (implicitCollections == null)
        implicitCollections = new HashMap(); 
      Collection collection = (Collection)implicitCollections.get(fieldName);
      if (collection == null) {
        Class fieldType = this.mapper.defaultImplementationOf(this.reflectionProvider.getFieldType(result, fieldName, null));
        if (!Collection.class.isAssignableFrom(fieldType))
          throw new ObjectAccessException("Field " + fieldName + " of " + result.getClass().getName() + " is configured for an implicit Collection, but field is of type " + fieldType
              .getName()); 
        if (this.pureJavaReflectionProvider == null)
          this.pureJavaReflectionProvider = new PureJavaReflectionProvider(); 
        collection = (Collection)this.pureJavaReflectionProvider.newInstance(fieldType);
        this.reflectionProvider.writeField(result, fieldName, collection, null);
        implicitCollections.put(fieldName, collection);
      } 
      collection.add(value);
    } 
    return implicitCollections;
  }
  
  private Class determineWhichClassDefinesField(HierarchicalStreamReader reader) {
    String definedIn = reader.getAttribute(this.mapper.aliasForAttribute("defined-in"));
    return (definedIn == null) ? null : this.mapper.realClass(definedIn);
  }
  
  protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String readResolveValue = reader.getAttribute(this.mapper.aliasForAttribute("resolves-to"));
    Class type = (readResolveValue != null) ? this.mapper.realClass(readResolveValue) : context.getRequiredType();
    Object currentObject = context.currentObject();
    if (currentObject != null && 
      type.isInstance(currentObject))
      return currentObject; 
    return this.reflectionProvider.newInstance(type);
  }
  
  private Class determineType(HierarchicalStreamReader reader, boolean validField, Object result, String fieldName, Class definedInCls) {
    String classAttribute = reader.getAttribute(this.mapper.aliasForAttribute("class"));
    if (classAttribute != null) {
      Class specifiedType = this.mapper.realClass(classAttribute);
      Class fieldType = this.reflectionProvider.getFieldType(result, fieldName, definedInCls);
      if (fieldType.isAssignableFrom(specifiedType))
        return specifiedType; 
    } 
    if (!validField) {
      Class itemType = this.mapper.getItemTypeForItemFieldName(result.getClass(), fieldName);
      if (itemType != null)
        return itemType; 
      return this.mapper.realClass(reader.getNodeName());
    } 
    Class fieldType = this.reflectionProvider.getFieldType(result, fieldName, definedInCls);
    return this.mapper.defaultImplementationOf(fieldType);
  }
  
  private Object readResolve() {
    this.serializationMethodInvoker = new SerializationMembers();
    return this;
  }
  
  private static final Logger LOGGER = Logger.getLogger(RobustReflectionConverter.class.getName());
}
