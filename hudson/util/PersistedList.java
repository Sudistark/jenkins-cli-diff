package hudson.util;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.model.Saveable;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@BridgeMethodsAdded
public class PersistedList<T> extends AbstractList<T> {
  private static final Logger LOGGER = Logger.getLogger(PersistedList.class.getName());
  
  protected final CopyOnWriteList<T> data = new CopyOnWriteList();
  
  protected Saveable owner = Saveable.NOOP;
  
  protected PersistedList() {}
  
  protected PersistedList(Collection<? extends T> initialList) { this.data.replaceBy(initialList); }
  
  public PersistedList(Saveable owner) { setOwner(owner); }
  
  public void setOwner(Saveable owner) { this.owner = owner; }
  
  @WithBridgeMethods({V.class})
  public boolean add(T item) {
    this.data.add(item);
    _onModified();
    return true;
  }
  
  @WithBridgeMethods({V.class})
  public boolean addAll(Collection<? extends T> items) {
    this.data.addAll(items);
    _onModified();
    return true;
  }
  
  public void replaceBy(Collection<? extends T> col) {
    this.data.replaceBy(col);
    onModified();
  }
  
  public T get(int index) { return (T)this.data.get(index); }
  
  public <U extends T> U get(Class<U> type) {
    for (T t : this.data) {
      if (type.isInstance(t))
        return (U)type.cast(t); 
    } 
    return null;
  }
  
  public <U extends T> List<U> getAll(Class<U> type) {
    List<U> r = new ArrayList<U>();
    for (T t : this.data) {
      if (type.isInstance(t))
        r.add(type.cast(t)); 
    } 
    return r;
  }
  
  public int size() { return this.data.size(); }
  
  public void remove(Class<? extends T> type) throws IOException {
    for (T t : this.data) {
      if (t.getClass() == type) {
        this.data.remove(t);
        onModified();
        return;
      } 
    } 
  }
  
  public void replace(T from, T to) throws IOException {
    List<T> copy = new ArrayList<T>(this.data.getView());
    for (int i = 0; i < copy.size(); i++) {
      if (copy.get(i).equals(from))
        copy.set(i, to); 
    } 
    this.data.replaceBy(copy);
  }
  
  public boolean remove(Object o) {
    boolean b = this.data.remove(o);
    if (b)
      _onModified(); 
    return b;
  }
  
  public void removeAll(Class<? extends T> type) throws IOException {
    boolean modified = false;
    for (T t : this.data) {
      if (t.getClass() == type) {
        this.data.remove(t);
        modified = true;
      } 
    } 
    if (modified)
      onModified(); 
  }
  
  public void clear() { this.data.clear(); }
  
  public Iterator<T> iterator() { return this.data.iterator(); }
  
  protected void onModified() {
    try {
      this.owner.save();
    } catch (IOException x) {
      Optional<T> ignored = stream().filter(PersistedList::ignoreSerializationErrors).findAny();
      if (ignored.isPresent()) {
        LOGGER.log(Level.WARNING, "Ignoring serialization errors in " + ignored.get() + "; update your parent POM to 4.8 or newer", x);
      } else {
        throw x;
      } 
    } 
  }
  
  private static final Set<String> IGNORED_CLASSES = Set.of("org.jvnet.hudson.test.TestBuilder", "org.jvnet.hudson.test.TestNotifier");
  
  private static boolean ignoreSerializationErrors(Object o) {
    if (o != null)
      for (Class<?> c = o.getClass(); c != Object.class; c = c.getSuperclass()) {
        if (IGNORED_CLASSES.contains(c.getName()))
          return true; 
      }  
    return false;
  }
  
  private void _onModified() {
    try {
      onModified();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public List<T> toList() { return this.data.getView(); }
  
  public <X> X[] toArray(X[] array) { return (X[])this.data.toArray(array); }
  
  public void addAllTo(Collection<? super T> dst) { this.data.addAllTo(dst); }
  
  public boolean isEmpty() { return this.data.isEmpty(); }
  
  public boolean contains(Object item) { return this.data.contains(item); }
  
  public String toString() { return toList().toString(); }
}
