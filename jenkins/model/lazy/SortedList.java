package jenkins.model.lazy;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SortedList<T extends Comparable<T>> extends AbstractList<T> {
  private List<T> data;
  
  SortedList(List<T> data) {
    this.data = new ArrayList(data);
    assert isSorted();
  }
  
  public int find(T probe) { return Collections.binarySearch(this.data, probe); }
  
  public boolean contains(Object o) { return (find((Comparable)o) >= 0); }
  
  public T get(int idx) { return (T)(Comparable)this.data.get(idx); }
  
  public int size() { return this.data.size(); }
  
  public T remove(int index) { return (T)(Comparable)this.data.remove(index); }
  
  public boolean remove(Object o) { return this.data.remove(o); }
  
  public int lower(T v) { return Boundary.LOWER.apply(find(v)); }
  
  public int higher(T v) { return Boundary.HIGHER.apply(find(v)); }
  
  public int floor(T v) { return Boundary.FLOOR.apply(find(v)); }
  
  public int ceil(T v) { return Boundary.CEIL.apply(find(v)); }
  
  public boolean isInRange(int idx) { return (0 <= idx && idx < this.data.size()); }
  
  private boolean isSorted() {
    for (int i = 1; i < this.data.size(); i++) {
      if (((Comparable)this.data.get(i)).compareTo((Comparable)this.data.get(i - 1)) < 0)
        return false; 
    } 
    return true;
  }
}
