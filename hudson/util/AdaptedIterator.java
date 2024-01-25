package hudson.util;

import java.util.Iterator;

public abstract class AdaptedIterator<T, U> extends Object implements Iterator<U> {
  private final Iterator<? extends T> core;
  
  protected AdaptedIterator(Iterator<? extends T> core) { this.core = core; }
  
  protected AdaptedIterator(Iterable<? extends T> core) { this(core.iterator()); }
  
  public boolean hasNext() { return this.core.hasNext(); }
  
  public U next() { return (U)adapt(this.core.next()); }
  
  protected abstract U adapt(T paramT);
  
  public void remove() { this.core.remove(); }
}
