package jenkins.model.lazy;

import java.util.AbstractList;
import java.util.Arrays;

class SortedIntList extends AbstractList<Integer> {
  private int[] data;
  
  private int size;
  
  SortedIntList(int capacity) {
    this.data = new int[capacity];
    this.size = 0;
  }
  
  SortedIntList(SortedIntList that) {
    this.data = new int[that.size + 8];
    System.arraycopy(that.data, 0, this.data, 0, that.size);
    this.size = that.size;
  }
  
  public int find(int probe) { return Arrays.binarySearch(this.data, 0, this.size, probe); }
  
  public boolean contains(Object o) { return (o instanceof Integer && contains(((Integer)o).intValue())); }
  
  public boolean contains(int i) { return (find(i) >= 0); }
  
  public Integer get(int index) {
    if (this.size <= index)
      throw new IndexOutOfBoundsException(); 
    return Integer.valueOf(this.data[index]);
  }
  
  public int size() { return this.size; }
  
  public int max() { return (this.size > 0) ? this.data[this.size - 1] : 0; }
  
  public boolean add(Integer i) { return add(i.intValue()); }
  
  public boolean add(int i) {
    ensureCapacity(this.size + 1);
    this.data[this.size++] = i;
    return true;
  }
  
  private void ensureCapacity(int i) {
    if (this.data.length < i) {
      int[] r = new int[Math.max(this.data.length * 2, i)];
      System.arraycopy(this.data, 0, r, 0, this.size);
      this.data = r;
    } 
  }
  
  public int lower(int v) { return Boundary.LOWER.apply(find(v)); }
  
  public int higher(int v) { return Boundary.HIGHER.apply(find(v)); }
  
  public int floor(int v) { return Boundary.FLOOR.apply(find(v)); }
  
  public int ceil(int v) { return Boundary.CEIL.apply(find(v)); }
  
  public boolean isInRange(int idx) { return (0 <= idx && idx < this.size); }
  
  public void sort() { Arrays.sort(this.data, 0, this.size); }
  
  public void copyInto(int[] dest) { System.arraycopy(this.data, 0, dest, 0, this.size); }
  
  public void removeValue(int n) {
    int idx = find(n);
    if (idx < 0)
      return; 
    System.arraycopy(this.data, idx + 1, this.data, idx, this.size - idx + 1);
    this.size--;
  }
}
