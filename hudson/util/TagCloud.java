package hudson.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TagCloud<T> extends AbstractList<TagCloud<T>.Entry> {
  private final List<Entry> entries = new ArrayList();
  
  private float max = 1.0F;
  
  public TagCloud(Iterable<? extends T> inputs, WeightFunction<T> f) {
    for (T input : inputs) {
      float w = Math.abs(f.weight(input));
      this.max = Math.max(w, this.max);
      this.entries.add(new Entry(this, input, w));
    } 
  }
  
  public Entry get(int index) { return (Entry)this.entries.get(index); }
  
  public int size() { return this.entries.size(); }
}
