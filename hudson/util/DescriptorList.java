package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;

public final class DescriptorList<T extends Describable<T>> extends AbstractList<Descriptor<T>> {
  private final Class<T> type;
  
  private final CopyOnWriteArrayList<Descriptor<T>> legacy;
  
  @Deprecated
  public DescriptorList(Descriptor... descriptors) {
    this.type = null;
    this.legacy = new CopyOnWriteArrayList(descriptors);
  }
  
  public DescriptorList(Class<T> type) {
    this.type = type;
    this.legacy = null;
  }
  
  public Descriptor<T> get(int index) { return (Descriptor)store().get(index); }
  
  public int size() { return store().size(); }
  
  public Iterator<Descriptor<T>> iterator() { return store().iterator(); }
  
  @Deprecated
  public boolean add(Descriptor<T> d) { return store().add(d); }
  
  @Deprecated
  public void add(int index, Descriptor<T> element) { add(element); }
  
  public boolean remove(Object o) { return store().remove(o); }
  
  private List<Descriptor<T>> store() {
    if (this.type == null)
      return this.legacy; 
    return Jenkins.get().getDescriptorList(this.type);
  }
  
  @CheckForNull
  public T newInstanceFromRadioList(JSONObject config) throws Descriptor.FormException {
    if (config.isNullObject())
      return null; 
    int idx = config.getInt("value");
    return (T)get(idx).newInstance(Stapler.getCurrentRequest(), config);
  }
  
  @CheckForNull
  public T newInstanceFromRadioList(JSONObject parent, String name) throws Descriptor.FormException {
    try {
      return (T)newInstanceFromRadioList(parent.getJSONObject(name));
    } catch (JSONException ex) {
      throw new Descriptor.FormException(ex, name);
    } 
  }
  
  @CheckForNull
  public Descriptor<T> findByName(String id) {
    for (Descriptor<T> d : this) {
      if (d.getId().equals(id))
        return d; 
    } 
    return null;
  }
  
  public void load(Class<? extends Describable> c) {
    try {
      Class.forName(c.getName(), true, c.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Deprecated
  @CheckForNull
  public Descriptor<T> find(String fqcn) { return Descriptor.find(this, fqcn); }
}
