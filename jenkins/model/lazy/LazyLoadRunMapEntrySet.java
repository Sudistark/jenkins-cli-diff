package jenkins.model.lazy;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

class LazyLoadRunMapEntrySet<R> extends AbstractSet<Map.Entry<Integer, R>> {
  private final AbstractLazyLoadRunMap<R> owner;
  
  private Set<Map.Entry<Integer, R>> all;
  
  LazyLoadRunMapEntrySet(AbstractLazyLoadRunMap<R> owner) { this.owner = owner; }
  
  private Set<Map.Entry<Integer, R>> all() {
    if (this.all == null)
      this.all = (new BuildReferenceMapAdapter(this.owner, this.owner.all())).entrySet(); 
    return this.all;
  }
  
  void clearCache() { this.all = null; }
  
  public int size() { return all().size(); }
  
  public boolean isEmpty() { return (this.owner.newestBuild() == null); }
  
  public boolean contains(Object o) {
    if (o instanceof Map.Entry) {
      Map.Entry<?, ?> e = (Map.Entry)o;
      Object k = e.getKey();
      if (k instanceof Integer)
        return this.owner.getByNumber(((Integer)k).intValue()).equals(e.getValue()); 
    } 
    return false;
  }
  
  public Iterator<Map.Entry<Integer, R>> iterator() { return new Object(this); }
  
  public Spliterator<Map.Entry<Integer, R>> spliterator() {
    return Spliterators.spliteratorUnknownSize(
        iterator(), 17);
  }
  
  public Object[] toArray() { return all().toArray(); }
  
  public <T> T[] toArray(T[] a) { return (T[])all().toArray(a); }
  
  public boolean add(Map.Entry<Integer, R> integerREntry) { throw new UnsupportedOperationException(); }
  
  public boolean remove(Object o) {
    if (o instanceof Map.Entry) {
      Map.Entry e = (Map.Entry)o;
      return this.owner.removeValue(e.getValue());
    } 
    return false;
  }
}
