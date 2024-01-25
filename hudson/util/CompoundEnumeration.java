package hudson.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompoundEnumeration<T> extends Object implements Enumeration<T> {
  private final Iterator<Enumeration<? extends T>> base;
  
  private Enumeration<? extends T> cur;
  
  public CompoundEnumeration(Enumeration... e) { this(Arrays.asList(e)); }
  
  public CompoundEnumeration(Iterable<Enumeration<? extends T>> e) {
    this.base = e.iterator();
    if (this.base.hasNext()) {
      this.cur = (Enumeration)this.base.next();
    } else {
      this.cur = Collections.emptyEnumeration();
    } 
  }
  
  public boolean hasMoreElements() {
    while (!this.cur.hasMoreElements() && this.base.hasNext())
      this.cur = (Enumeration)this.base.next(); 
    return this.cur.hasMoreElements();
  }
  
  public T nextElement() throws NoSuchElementException { return (T)this.cur.nextElement(); }
}
