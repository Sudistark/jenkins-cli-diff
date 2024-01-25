package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.Publisher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.ExtensionComponentSet;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;

public class DescriptorExtensionList<T extends Describable<T>, D extends Descriptor<T>> extends ExtensionList<D> {
  private final Class<T> describableType;
  
  public static <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> createDescriptorList(Jenkins jenkins, Class<T> describableType) {
    if (describableType == Publisher.class)
      return new Publisher.DescriptorExtensionListImpl(jenkins); 
    return new DescriptorExtensionList(jenkins, describableType);
  }
  
  @Deprecated
  public static <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> createDescriptorList(Hudson hudson, Class<T> describableType) { return createDescriptorList(hudson, describableType); }
  
  @Deprecated
  protected DescriptorExtensionList(Hudson hudson, Class<T> describableType) { this(hudson, describableType); }
  
  protected DescriptorExtensionList(Jenkins jenkins, Class<T> describableType) {
    super(jenkins, Descriptor.class, getLegacyDescriptors(describableType));
    this.describableType = describableType;
  }
  
  @Deprecated
  public D find(String fqcn) { return (D)Descriptor.find(this, fqcn); }
  
  public D find(Class<? extends T> type) {
    for (Iterator iterator = iterator(); iterator.hasNext(); ) {
      D d = (D)(Descriptor)iterator.next();
      if (d.clazz == type)
        return d; 
    } 
    return null;
  }
  
  @CheckForNull
  public T newInstanceFromRadioList(JSONObject config) throws Descriptor.FormException {
    if (config.isNullObject())
      return null; 
    int idx = config.getInt("value");
    return (T)((Descriptor)get(idx)).newInstance(Stapler.getCurrentRequest(), config);
  }
  
  @CheckForNull
  public T newInstanceFromRadioList(@NonNull JSONObject parent, @NonNull String name) throws Descriptor.FormException {
    try {
      return (T)newInstanceFromRadioList(parent.getJSONObject(name));
    } catch (JSONException ex) {
      throw new Descriptor.FormException(ex, name);
    } 
  }
  
  @CheckForNull
  public D findByName(String id) {
    for (Iterator iterator = iterator(); iterator.hasNext(); ) {
      D d = (D)(Descriptor)iterator.next();
      if (d.getId().equals(id))
        return d; 
    } 
    return null;
  }
  
  public boolean add(D d) {
    boolean r = super.add(d);
    getDescriptorExtensionList().add(d);
    return r;
  }
  
  public boolean remove(Object o) {
    getDescriptorExtensionList().remove(o);
    return super.remove(o);
  }
  
  protected Object getLoadLock() { return getDescriptorExtensionList().getLoadLock(); }
  
  protected List<ExtensionComponent<D>> load() {
    if (this.jenkins == null) {
      LOGGER.log(Level.WARNING, "Cannot load extension components, because Jenkins instance has not been assigned yet");
      return Collections.emptyList();
    } 
    return _load(getDescriptorExtensionList().getComponents());
  }
  
  protected Collection<ExtensionComponent<D>> load(ExtensionComponentSet delta) { return _load(delta.find(Descriptor.class)); }
  
  private List<ExtensionComponent<D>> _load(Iterable<ExtensionComponent<Descriptor>> set) {
    List<ExtensionComponent<D>> r = new ArrayList<ExtensionComponent<D>>();
    for (ExtensionComponent<Descriptor> c : set) {
      Descriptor d = (Descriptor)c.getInstance();
      try {
        if (d.getT() == this.describableType)
          r.add(c); 
      } catch (IllegalStateException e) {
        LOGGER.log(Level.SEVERE, "" + d.getClass() + " doesn't extend Descriptor with a type parameter", e);
      } 
    } 
    return r;
  }
  
  private ExtensionList<Descriptor> getDescriptorExtensionList() { return ExtensionList.lookup(Descriptor.class); }
  
  private static final Map<Class, CopyOnWriteArrayList<ExtensionComponent<Descriptor>>> legacyDescriptors = new ConcurrentHashMap();
  
  private static <T extends Describable<T>> CopyOnWriteArrayList<ExtensionComponent<Descriptor>> getLegacyDescriptors(Class<T> type) { return (CopyOnWriteArrayList)legacyDescriptors.computeIfAbsent(type, key -> new CopyOnWriteArrayList()); }
  
  public static Iterable<Descriptor> listLegacyInstances() { return new Object(); }
  
  public static void clearLegacyInstances() { legacyDescriptors.clear(); }
  
  private static final Logger LOGGER = Logger.getLogger(DescriptorExtensionList.class.getName());
}
