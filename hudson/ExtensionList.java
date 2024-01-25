package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.init.InitMilestone;
import hudson.model.Hudson;
import hudson.util.Iterators;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.ExtensionComponentSet;
import jenkins.model.Jenkins;
import jenkins.util.io.OnMaster;

public class ExtensionList<T> extends AbstractList<T> implements OnMaster {
  @Deprecated
  public final Hudson hudson;
  
  @CheckForNull
  public final Jenkins jenkins;
  
  public final Class<T> extensionType;
  
  private final List<ExtensionListListener> listeners;
  
  private final CopyOnWriteArrayList<ExtensionComponent<T>> legacyInstances;
  
  @Deprecated
  protected ExtensionList(Hudson hudson, Class<T> extensionType) { this(hudson, extensionType); }
  
  protected ExtensionList(Jenkins jenkins, Class<T> extensionType) { this(jenkins, extensionType, new CopyOnWriteArrayList()); }
  
  @Deprecated
  protected ExtensionList(Hudson hudson, Class<T> extensionType, CopyOnWriteArrayList<ExtensionComponent<T>> legacyStore) { this(hudson, extensionType, legacyStore); }
  
  protected ExtensionList(Jenkins jenkins, Class<T> extensionType, CopyOnWriteArrayList<ExtensionComponent<T>> legacyStore) {
    this.listeners = new CopyOnWriteArrayList();
    this.hudson = (Hudson)jenkins;
    this.jenkins = jenkins;
    this.extensionType = extensionType;
    this.legacyInstances = legacyStore;
    if (jenkins == null)
      this.extensions = Collections.emptyList(); 
  }
  
  public void addListener(@NonNull ExtensionListListener listener) { this.listeners.add(listener); }
  
  @CheckForNull
  public <U extends T> U get(@NonNull Class<U> type) {
    for (T ext : this) {
      if (ext.getClass() == type)
        return (U)type.cast(ext); 
    } 
    return null;
  }
  
  @NonNull
  public <U extends T> U getInstance(@NonNull Class<U> type) {
    for (T ext : this) {
      if (ext.getClass() == type)
        return (U)type.cast(ext); 
    } 
    throw new IllegalStateException("The class " + type.getName() + " was not found, potentially not yet loaded");
  }
  
  @NonNull
  public Iterator<T> iterator() { return new Object(this, Iterators.readOnly(ensureLoaded().iterator())); }
  
  public List<ExtensionComponent<T>> getComponents() { return Collections.unmodifiableList(ensureLoaded()); }
  
  public T get(int index) { return (T)((ExtensionComponent)ensureLoaded().get(index)).getInstance(); }
  
  public int size() { return ensureLoaded().size(); }
  
  public List<T> reverseView() { return new Object(this); }
  
  public boolean remove(Object o) {
    try {
      return removeSync(o);
    } finally {
      if (this.extensions != null)
        fireOnChangeListeners(); 
    } 
  }
  
  public boolean removeAll(Collection<?> c) {
    removed = false;
    try {
      for (Object o : c)
        removed |= removeSync(o); 
      return removed;
    } finally {
      if (this.extensions != null && removed)
        fireOnChangeListeners(); 
    } 
  }
  
  private boolean removeSync(Object o) {
    boolean removed = removeComponent(this.legacyInstances, o);
    if (this.extensions != null) {
      List<ExtensionComponent<T>> r = new ArrayList<ExtensionComponent<T>>(this.extensions);
      removed |= removeComponent(r, o);
      this.extensions = sort(r);
    } 
    return removed;
  }
  
  private boolean removeComponent(Collection<ExtensionComponent<T>> collection, Object t) {
    for (ExtensionComponent<T> c : collection) {
      if (c.getInstance().equals(t))
        return collection.remove(c); 
    } 
    return false;
  }
  
  public final T remove(int index) {
    T t = (T)get(index);
    remove(t);
    return t;
  }
  
  @Deprecated
  public boolean add(T t) {
    try {
      return addSync(t);
    } finally {
      if (this.extensions != null)
        fireOnChangeListeners(); 
    } 
  }
  
  private boolean addSync(T t) {
    this.legacyInstances.add(new ExtensionComponent(t));
    if (this.extensions != null) {
      List<ExtensionComponent<T>> r = new ArrayList<ExtensionComponent<T>>(this.extensions);
      r.add(new ExtensionComponent(t));
      this.extensions = sort(r);
    } 
    return true;
  }
  
  public void add(int index, T element) { add(element); }
  
  public T getDynamic(String className) {
    for (T t : this) {
      if (t.getClass().getName().equals(className))
        return t; 
    } 
    return null;
  }
  
  private List<ExtensionComponent<T>> ensureLoaded() {
    if (this.extensions != null)
      return this.extensions; 
    if (this.jenkins == null || this.jenkins.getInitLevel().compareTo(InitMilestone.PLUGINS_PREPARED) < 0)
      return this.legacyInstances; 
    synchronized (getLoadLock()) {
      if (this.extensions == null) {
        List<ExtensionComponent<T>> r = load();
        r.addAll(this.legacyInstances);
        this.extensions = sort(r);
      } 
      return this.extensions;
    } 
  }
  
  protected Object getLoadLock() { return ((Jenkins)Objects.requireNonNull(this.jenkins)).lookup.setIfNull(Lock.class, new Lock()); }
  
  public void refresh(ExtensionComponentSet delta) {
    boolean fireOnChangeListeners = false;
    synchronized (getLoadLock()) {
      if (this.extensions == null)
        return; 
      Collection<ExtensionComponent<T>> found = load(delta);
      if (!found.isEmpty()) {
        List<ExtensionComponent<T>> l = new ArrayList<ExtensionComponent<T>>(this.extensions);
        l.addAll(found);
        this.extensions = sort(l);
        fireOnChangeListeners = true;
      } 
    } 
    if (fireOnChangeListeners)
      fireOnChangeListeners(); 
  }
  
  private void fireOnChangeListeners() {
    for (ExtensionListListener listener : this.listeners) {
      try {
        listener.onChange();
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Error firing ExtensionListListener.onChange().", e);
      } 
    } 
  }
  
  protected List<ExtensionComponent<T>> load() {
    LOGGER.fine(() -> String.format("Loading ExtensionList '%s'", new Object[] { this.extensionType.getName() }));
    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.log(Level.FINER, String.format("Loading ExtensionList '%s' from", new Object[] { this.extensionType.getName() }), new Throwable("Only present for stacktrace information")); 
    return ((Jenkins)Objects.requireNonNull(this.jenkins)).getPluginManager().getPluginStrategy().findComponents(this.extensionType, this.hudson);
  }
  
  protected Collection<ExtensionComponent<T>> load(ExtensionComponentSet delta) { return delta.find(this.extensionType); }
  
  protected List<ExtensionComponent<T>> sort(List<ExtensionComponent<T>> r) {
    r = new ArrayList<ExtensionComponent<T>>(r);
    Collections.sort(r);
    return r;
  }
  
  @Deprecated
  public static <T> ExtensionList<T> create(Hudson hudson, Class<T> type) { return create(hudson, type); }
  
  public static <T> ExtensionList<T> create(Jenkins jenkins, Class<T> type) {
    if (type.getAnnotation(ExtensionPoint.LegacyInstancesAreScopedToHudson.class) != null)
      return new ExtensionList(jenkins, type); 
    return new ExtensionList(jenkins, type, (CopyOnWriteArrayList)staticLegacyInstances.computeIfAbsent(type, key -> new CopyOnWriteArrayList()));
  }
  
  @NonNull
  public static <T> ExtensionList<T> lookup(Class<T> type) {
    Jenkins j = Jenkins.getInstanceOrNull();
    return (j == null) ? create((Jenkins)null, type) : j.getExtensionList(type);
  }
  
  @NonNull
  public static <U> U lookupSingleton(Class<U> type) {
    ExtensionList<U> all = lookup(type);
    if (all.size() != 1)
      throw new IllegalStateException("Expected 1 instance of " + type.getName() + " but got " + all.size()); 
    return (U)all.get(0);
  }
  
  private static final Map<Class, CopyOnWriteArrayList> staticLegacyInstances = new ConcurrentHashMap();
  
  public static void clearLegacyInstances() { staticLegacyInstances.clear(); }
  
  private static final Logger LOGGER = Logger.getLogger(ExtensionList.class.getName());
}
