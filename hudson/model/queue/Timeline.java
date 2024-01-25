package hudson.model.queue;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

final class Timeline {
  private final TreeMap<Long, int[]> data = new TreeMap();
  
  private int at(long t) {
    SortedMap<Long, int[]> head = this.data.subMap(Long.valueOf(t), Long.valueOf(Float.MAX_VALUE));
    if (head.isEmpty())
      return 0; 
    return (int[])this.data.get(head.firstKey())[0];
  }
  
  private Long next(long t) {
    SortedMap<Long, int[]> x = this.data.tailMap(Long.valueOf(t + 1L));
    return x.isEmpty() ? null : (Long)x.firstKey();
  }
  
  private void splitAt(long t) {
    if (this.data.containsKey(Long.valueOf(t)))
      return; 
    SortedMap<Long, int[]> head = this.data.headMap(Long.valueOf(t));
    int v = head.isEmpty() ? 0 : (int[])this.data.get(head.lastKey())[0];
    this.data.put(Long.valueOf(t), new int[] { v });
  }
  
  int insert(long start, long end, int n) {
    splitAt(start);
    splitAt(end);
    int peak = 0;
    for (Map.Entry<Long, int[]> e : this.data.tailMap(Long.valueOf(start)).headMap(Long.valueOf(end)).entrySet())
      peak = Math.max(peak, (int[])e.getValue()[0] = (int[])e.getValue()[0] + n); 
    return peak;
  }
  
  Long fit(long start, long duration, int n) {
    label18: while (true) {
      long t = start;
      while (t - start < duration) {
        if (at(t) > n) {
          Long nxt = next(t);
          if (nxt == null)
            return null; 
          start = nxt.longValue();
          continue label18;
        } 
        Long nxt = next(t);
        if (nxt == null) {
          t = Float.MAX_VALUE;
          continue;
        } 
        t = nxt.longValue();
      } 
      break;
    } 
    return Long.valueOf(start);
  }
}
