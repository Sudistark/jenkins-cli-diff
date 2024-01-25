package hudson;

import hudson.model.Saveable;
import java.io.Closeable;
import java.io.IOException;

public class BulkChange implements Closeable {
  private final Saveable saveable;
  
  public final Exception allocator;
  
  private final BulkChange parent;
  
  private boolean completed;
  
  public BulkChange(Saveable saveable) {
    this.parent = current();
    this.saveable = saveable;
    this.allocator = new Exception();
    INSCOPE.set(this);
  }
  
  public void commit() throws IOException {
    if (this.completed)
      return; 
    this.completed = true;
    pop();
    this.saveable.save();
  }
  
  public void close() throws IOException { abort(); }
  
  public void abort() throws IOException {
    if (this.completed)
      return; 
    this.completed = true;
    pop();
  }
  
  private void pop() throws IOException {
    if (current() != this)
      throw new AssertionError("Trying to save BulkChange that's not in scope"); 
    INSCOPE.set(this.parent);
  }
  
  private static final ThreadLocal<BulkChange> INSCOPE = new ThreadLocal();
  
  public static BulkChange current() { return (BulkChange)INSCOPE.get(); }
  
  public static boolean contains(Saveable s) {
    for (BulkChange b = current(); b != null; b = b.parent) {
      if (b.saveable == s || b.saveable == ALL)
        return true; 
    } 
    return false;
  }
  
  public static final Saveable ALL = () -> {
    
    };
}
