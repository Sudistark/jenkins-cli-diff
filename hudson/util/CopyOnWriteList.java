package hudson.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CopyOnWriteList<E> extends Object implements Iterable<E> {
  public CopyOnWriteList(List<E> core) { this(core, false); }
  
  private CopyOnWriteList(List<E> core, boolean noCopy) { this.core = noCopy ? core : new ArrayList(core); }
  
  public CopyOnWriteList() { this.core = Collections.emptyList(); }
  
  public void add(E e) {
    List<E> n = new ArrayList<E>(this.core);
    n.add(e);
    this.core = n;
  }
  
  public void addAll(Collection<? extends E> items) {
    List<E> n = new ArrayList<E>(this.core);
    n.addAll(items);
    this.core = n;
  }
  
  public boolean remove(E e) {
    List<E> n = new ArrayList<E>(this.core);
    boolean r = n.remove(e);
    this.core = n;
    return r;
  }
  
  public Iterator<E> iterator() {
    Iterator<? extends E> itr = this.core.iterator();
    return new Object(this, itr);
  }
  
  public void replaceBy(CopyOnWriteList<? extends E> that) { this.core = that.core; }
  
  public void replaceBy(Collection<? extends E> that) { this.core = new ArrayList(that); }
  
  public void replaceBy(E... that) { replaceBy(Arrays.asList(that)); }
  
  public void clear() { this.core = new ArrayList(); }
  
  public <T> T[] toArray(T[] array) { return (T[])this.core.toArray(array); }
  
  public List<E> getView() { return Collections.unmodifiableList(this.core); }
  
  public void addAllTo(Collection<? super E> dst) { dst.addAll(this.core); }
  
  public E get(int index) { return (E)this.core.get(index); }
  
  public boolean isEmpty() { return this.core.isEmpty(); }
  
  public int size() { return this.core.size(); }
  
  public boolean contains(Object item) { return this.core.contains(item); }
  
  public String toString() { return this.core.toString(); }
}
