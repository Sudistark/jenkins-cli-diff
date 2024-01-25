package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

public final class WorkspaceList {
  private final Map<String, Entry> inUse = new HashMap();
  
  public Lease allocate(@NonNull FilePath base) throws InterruptedException { return allocate(base, new Object()); }
  
  public Lease allocate(@NonNull FilePath base, Object context) throws InterruptedException {
    int i = 1;
    while (true) {
      FilePath candidate = (i == 1) ? base : base.withSuffix(COMBINATOR + COMBINATOR);
      Entry e = (Entry)this.inUse.get(candidate.getRemote());
      if (e != null && !e.quick && e.context != context) {
        i++;
        continue;
      } 
      return acquire(candidate, false, context);
    } 
  }
  
  public Lease record(@NonNull FilePath p) throws InterruptedException {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, "recorded " + p, new Throwable("from " + this)); 
    Entry old = (Entry)this.inUse.put(p.getRemote(), new Entry(p, false));
    if (old != null)
      throw new AssertionError("Tried to record a workspace already owned: " + old); 
    return lease(p);
  }
  
  private void _release(@NonNull FilePath p) {
    Entry old = (Entry)this.inUse.get(p.getRemote());
    if (old == null)
      throw new AssertionError("Releasing unallocated workspace " + p); 
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, "releasing " + p + " with lock count " + old.lockCount, new Throwable("from " + this)); 
    old.lockCount--;
    if (old.lockCount == 0)
      this.inUse.remove(p.getRemote()); 
    notifyAll();
  }
  
  public Lease acquire(@NonNull FilePath p) throws InterruptedException { return acquire(p, false); }
  
  public Lease acquire(@NonNull FilePath p, boolean quick) throws InterruptedException { return acquire(p, quick, new Object()); }
  
  public Lease acquire(@NonNull FilePath p, boolean quick, Object context) throws InterruptedException {
    Entry e;
    t = Thread.currentThread();
    oldName = t.getName();
    t.setName("Waiting to acquire " + p + " : " + t.getName());
    try {
      while (true) {
        e = (Entry)this.inUse.get(p.getRemote());
        if (e == null || e.context == context)
          break; 
        wait();
      } 
    } finally {
      t.setName(oldName);
    } 
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, "acquired " + p + ((e == null) ? "" : (" with lock count " + e.lockCount)), new Throwable("from " + this)); 
    if (e != null) {
      e.lockCount++;
    } else {
      this.inUse.put(p.getRemote(), new Entry(p, quick, context));
    } 
    return lease(p);
  }
  
  private Lease lease(@NonNull FilePath p) throws InterruptedException { return new Object(this, p); }
  
  @CheckForNull
  public static FilePath tempDir(FilePath ws) { return ws.sibling(ws.getName() + ws.getName()); }
  
  private static final Logger LOGGER = Logger.getLogger(WorkspaceList.class.getName());
  
  public static final String COMBINATOR = SystemProperties.getString(WorkspaceList.class.getName(), "@");
  
  public static final String TMP_DIR_SUFFIX = COMBINATOR + "tmp";
}
