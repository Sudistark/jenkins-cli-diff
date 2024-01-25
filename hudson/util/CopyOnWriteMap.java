package hudson.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class CopyOnWriteMap<K, V> extends Object implements Map<K, V> {
  protected CopyOnWriteMap(Map<K, V> core) { update(core); }
  
  protected CopyOnWriteMap() { update(Collections.emptyMap()); }
  
  protected void update(Map<K, V> m) {
    this.core = m;
    this.view = Collections.unmodifiableMap(this.core);
  }
  
  public void replaceBy(Map<? extends K, ? extends V> data) {
    Map<K, V> d = copy();
    d.clear();
    d.putAll(data);
    update(d);
  }
  
  public int size() { return this.core.size(); }
  
  public boolean isEmpty() { return this.core.isEmpty(); }
  
  public boolean containsKey(Object key) { return this.core.containsKey(key); }
  
  public boolean containsValue(Object value) { return this.core.containsValue(value); }
  
  public V get(Object key) { return (V)this.core.get(key); }
  
  public V put(K key, V value) {
    Map<K, V> m = copy();
    V r = (V)m.put(key, value);
    update(m);
    return r;
  }
  
  public V remove(Object key) {
    Map<K, V> m = copy();
    V r = (V)m.remove(key);
    update(m);
    return r;
  }
  
  public void putAll(Map<? extends K, ? extends V> t) {
    Map<K, V> m = copy();
    m.putAll(t);
    update(m);
  }
  
  protected abstract Map<K, V> copy();
  
  public void clear() { update(Collections.emptyMap()); }
  
  public Set<K> keySet() { return this.view.keySet(); }
  
  public Collection<V> values() { return this.view.values(); }
  
  public Set<Map.Entry<K, V>> entrySet() { return this.view.entrySet(); }
  
  public int hashCode() { return copy().hashCode(); }
  
  public boolean equals(Object obj) { return copy().equals(obj); }
  
  public String toString() { return copy().toString(); }
}
