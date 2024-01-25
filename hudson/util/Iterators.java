package hudson.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import org.kohsuke.accmod.Restricted;

public class Iterators {
  public static <T> Iterator<T> empty() { return Collections.emptyIterator(); }
  
  public static <T> Iterable<T> reverse(List<T> lst) {
    return () -> {
        ListIterator<T> itr = lst.listIterator(lst.size());
        return new Object(itr);
      };
  }
  
  public static <T> Iterable<T> wrap(Iterable<T> base) {
    return () -> {
        Iterator<T> itr = base.iterator();
        return new Object(itr);
      };
  }
  
  public static List<Integer> sequence(int start, int end, int step) {
    int size = (end - start) / step;
    if (size < 0)
      throw new IllegalArgumentException("List size is negative"); 
    return new Object(size, start, step);
  }
  
  public static List<Integer> sequence(int start, int end) { return sequence(start, end, 1); }
  
  public static List<Integer> reverseSequence(int start, int end, int step) { return sequence(end - 1, start - 1, -step); }
  
  public static List<Integer> reverseSequence(int start, int end) { return reverseSequence(start, end, 1); }
  
  public static <T> Iterator<T> cast(Iterator<? extends T> itr) { return itr; }
  
  public static <T> Iterable<T> cast(Iterable<? extends T> itr) { return itr; }
  
  public static <U, T extends U> Iterator<T> subType(Iterator<U> itr, Class<T> type) { return new Object(itr, type); }
  
  public static <T> Iterator<T> readOnly(Iterator<T> itr) { return new Object(itr); }
  
  public static <T> Iterator<T> removeNull(Iterator<T> itr) { return Iterators.filter(itr, Objects::nonNull); }
  
  @SafeVarargs
  public static <T> Iterable<T> sequence(Iterable... iterables) { return () -> new Object(ImmutableList.copyOf(iterables)); }
  
  public static <T> Iterator<T> removeDups(Iterator<T> iterator) { return new Object(iterator); }
  
  public static <T> Iterable<T> removeDups(Iterable<T> base) { return () -> removeDups(base.iterator()); }
  
  @SafeVarargs
  public static <T> Iterator<T> sequence(Iterator... iterators) { return Iterators.concat(iterators); }
  
  public static <T> Iterator<T> limit(Iterator<? extends T> base, CountingPredicate<? super T> filter) { return new Object(base, filter); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void skip(@NonNull Iterator<?> iterator, int count) {
    if (count < 0)
      throw new IllegalArgumentException(); 
    while (iterator.hasNext() && count-- > 0)
      iterator.next(); 
  }
}
