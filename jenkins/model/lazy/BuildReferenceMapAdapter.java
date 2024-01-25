package jenkins.model.lazy;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

class BuildReferenceMapAdapter<R> extends Object implements SortedMap<Integer, R> {
  private final AbstractLazyLoadRunMap<R> loader;
  
  private final SortedMap<Integer, BuildReference<R>> core;
  
  BuildReferenceMapAdapter(AbstractLazyLoadRunMap<R> loader, SortedMap<Integer, BuildReference<R>> core) {
    this.loader = loader;
    this.core = core;
  }
  
  private R unwrap(@Nullable BuildReference<R> ref) {
    if (ref == null)
      return null; 
    R v = (R)ref.get();
    if (v == null)
      v = (R)this.loader.getById(ref.id); 
    return v;
  }
  
  private BuildReference<R> wrap(@Nullable R value) {
    if (value == null)
      return null; 
    return this.loader.createReference(value);
  }
  
  public Comparator<? super Integer> comparator() { return this.core.comparator(); }
  
  public SortedMap<Integer, R> subMap(Integer fromKey, Integer toKey) { return new BuildReferenceMapAdapter(this.loader, this.core.subMap(fromKey, toKey)); }
  
  public SortedMap<Integer, R> headMap(Integer toKey) { return new BuildReferenceMapAdapter(this.loader, this.core.headMap(toKey)); }
  
  public SortedMap<Integer, R> tailMap(Integer fromKey) { return new BuildReferenceMapAdapter(this.loader, this.core.tailMap(fromKey)); }
  
  public Integer firstKey() { return (Integer)this.core.firstKey(); }
  
  public Integer lastKey() { return (Integer)this.core.lastKey(); }
  
  public Set<Integer> keySet() { return this.core.keySet(); }
  
  public Collection<R> values() { return new CollectionAdapter(this, this.core.values()); }
  
  public Set<Map.Entry<Integer, R>> entrySet() { return new SetAdapter(this, this.core.entrySet()); }
  
  public int size() { return this.core.size(); }
  
  public boolean isEmpty() { return this.core.isEmpty(); }
  
  public boolean containsKey(Object key) { return this.core.containsKey(key); }
  
  public boolean containsValue(Object value) { return this.core.containsValue(value); }
  
  public R get(Object key) { return (R)unwrap((BuildReference)this.core.get(key)); }
  
  public R put(Integer key, R value) { return (R)unwrap((BuildReference)this.core.put(key, wrap(value))); }
  
  public R remove(Object key) { return (R)unwrap((BuildReference)this.core.remove(key)); }
  
  public void putAll(Map<? extends Integer, ? extends R> m) {
    for (Map.Entry<? extends Integer, ? extends R> e : m.entrySet())
      put((Integer)e.getKey(), e.getValue()); 
  }
  
  public void clear() { this.core.clear(); }
  
  public boolean equals(Object o) { return this.core.equals(o); }
  
  public int hashCode() { return this.core.hashCode(); }
  
  public String toString() { return (new LinkedHashMap(this)).toString(); }
}
