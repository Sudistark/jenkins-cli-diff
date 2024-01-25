package hudson.util;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.ReconfigurableDescribable;
import hudson.model.Saveable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.DependencyDeclarer;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class DescribableList<T extends Describable<T>, D extends Descriptor<T>> extends PersistedList<T> {
  protected DescribableList() {}
  
  @Deprecated
  public DescribableList(Owner owner) { setOwner(owner); }
  
  public DescribableList(Saveable owner) { setOwner(owner); }
  
  public DescribableList(Saveable owner, Collection<? extends T> initialList) {
    super(initialList);
    setOwner(owner);
  }
  
  @Deprecated
  public void setOwner(Owner owner) { this.owner = owner; }
  
  public void replace(T item) throws IOException {
    for (Iterator iterator = this.data.iterator(); iterator.hasNext(); ) {
      T t = (T)(Describable)iterator.next();
      if (t.getClass() == item.getClass())
        this.data.remove(t); 
    } 
    this.data.add(item);
    onModified();
  }
  
  public T getDynamic(String id) {
    for (iterator = this.data.iterator(); iterator.hasNext(); ) {
      T t = (T)(Describable)iterator.next();
      if (t.getDescriptor().getId().equals(id))
        return t; 
    } 
    try {
      return (T)(Describable)this.data.get(Integer.parseInt(id));
    } catch (NumberFormatException iterator) {
      return null;
    } 
  }
  
  public T get(D descriptor) {
    for (Iterator iterator = this.data.iterator(); iterator.hasNext(); ) {
      T t = (T)(Describable)iterator.next();
      if (t.getDescriptor() == descriptor)
        return t; 
    } 
    return null;
  }
  
  public boolean contains(D d) { return (get(d) != null); }
  
  public void remove(D descriptor) throws IOException {
    for (Iterator iterator = this.data.iterator(); iterator.hasNext(); ) {
      T t = (T)(Describable)iterator.next();
      if (t.getDescriptor() == descriptor) {
        this.data.remove(t);
        onModified();
        return;
      } 
    } 
  }
  
  public Map<D, T> toMap() { return Descriptor.toMap(this.data); }
  
  public void rebuild(StaplerRequest req, JSONObject json, List<? extends Descriptor<T>> descriptors) throws Descriptor.FormException, IOException {
    List<T> newList = new ArrayList<T>();
    for (Descriptor<T> d : descriptors) {
      T existing = (T)get(d);
      String name = d.getJsonSafeClassName();
      JSONObject o = json.optJSONObject(name);
      T instance = null;
      if (o != null) {
        if (existing instanceof ReconfigurableDescribable) {
          instance = (T)((ReconfigurableDescribable)existing).reconfigure(req, o);
        } else {
          instance = (T)d.newInstance(req, o);
        } 
      } else if (existing instanceof ReconfigurableDescribable) {
        instance = (T)((ReconfigurableDescribable)existing).reconfigure(req, null);
      } 
      if (instance != null)
        newList.add(instance); 
    } 
    replaceBy(newList);
  }
  
  @Deprecated
  public void rebuild(StaplerRequest req, JSONObject json, List<? extends Descriptor<T>> descriptors, String prefix) throws Descriptor.FormException, IOException { rebuild(req, json, descriptors); }
  
  public void rebuildHetero(StaplerRequest req, JSONObject formData, Collection<? extends Descriptor<T>> descriptors, String key) throws Descriptor.FormException, IOException { replaceBy(Descriptor.newInstancesFromHeteroList(req, formData, key, descriptors)); }
  
  public void buildDependencyGraph(AbstractProject owner, DependencyGraph graph) {
    for (Object o : this) {
      if (o instanceof DependencyDeclarer) {
        DependencyDeclarer dd = (DependencyDeclarer)o;
        try {
          dd.buildDependencyGraph(owner, graph);
        } catch (RuntimeException e) {
          LOGGER.log(Level.SEVERE, "Failed to build dependency graph for " + owner, e);
        } 
      } 
    } 
  }
  
  public <U extends T> U get(Class<U> type) { return (U)(Describable)super.get(type); }
  
  public T[] toArray(T[] array) { return (T[])(Describable[])toArray(array); }
  
  private static final Logger LOGGER = Logger.getLogger(DescribableList.class.getName());
}
