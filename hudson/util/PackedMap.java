package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class PackedMap<K, V> extends AbstractMap<K, V> {
  private final Object[] kvpairs;
  
  private final Set<Map.Entry<K, V>> entrySet;
  
  public static <K, V> PackedMap<K, V> of(Map<? extends K, ? extends V> src) { return new PackedMap(src); }
  
  private PackedMap(Map<? extends K, ? extends V> src) {
    this.entrySet = new Object(this);
    this.kvpairs = new Object[src.size() * 2];
    int i = 0;
    for (Map.Entry<? extends K, ? extends V> e : src.entrySet()) {
      this.kvpairs[i++] = e.getKey();
      this.kvpairs[i++] = e.getValue();
    } 
  }
  
  public Set<Map.Entry<K, V>> entrySet() { return this.entrySet; }
  
  public boolean containsKey(Object key) {
    for (int i = 0; i < this.kvpairs.length; i += 2) {
      if (key.equals(this.kvpairs[i]))
        return true; 
    } 
    return false;
  }
  
  public V get(Object key) {
    for (int i = 0; i < this.kvpairs.length; i += 2) {
      if (key.equals(this.kvpairs[i]))
        return (V)this.kvpairs[i + 1]; 
    } 
    return null;
  }
  
  @NonNull
  public Collection<V> values() { return new Object(this); }
}
