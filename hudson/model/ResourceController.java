package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.remoting.Callable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;

public class ResourceController {
  private final Set<ResourceActivity> inProgress = new CopyOnWriteArraySet();
  
  private final Collection<ResourceList> resourceView = new Object(this);
  
  private ResourceList inUse = ResourceList.EMPTY;
  
  public void execute(@NonNull Runnable task, ResourceActivity activity) throws InterruptedException {
    ResourceList resources = activity.getResourceList();
    _withLock(new Object(this, resources, activity));
    try {
      task.run();
    } finally {
      _withLock(new Object(this, activity));
    } 
  }
  
  public boolean canRun(ResourceList resources) {
    try {
      return ((Boolean)_withLock(new Object(this, resources))).booleanValue();
    } catch (Exception e) {
      throw new IllegalStateException("Inner callable does not throw exception", e);
    } 
  }
  
  public Resource getMissingResource(ResourceList resources) {
    try {
      return (Resource)_withLock(new Object(this, resources));
    } catch (Exception e) {
      throw new IllegalStateException("Inner callable does not throw exception", e);
    } 
  }
  
  public ResourceActivity getBlockingActivity(ResourceActivity activity) {
    ResourceList res = activity.getResourceList();
    for (ResourceActivity a : this.inProgress) {
      if (res.isCollidingWith(a.getResourceList()))
        return a; 
    } 
    return null;
  }
  
  @SuppressFBWarnings(value = {"WA_NOT_IN_LOOP"}, justification = "the caller does indeed call this method in a loop")
  protected void _await() { wait(); }
  
  protected void _signalAll() { notifyAll(); }
  
  protected void _withLock(Runnable runnable) {
    synchronized (this) {
      runnable.run();
    } 
  }
  
  protected <V> V _withLock(Callable<V> callable) throws Exception {
    synchronized (this) {
      return (V)callable.call();
    } 
  }
  
  protected <V, T extends Throwable> V _withLock(Callable<V, T> callable) throws T {
    synchronized (this) {
      return (V)callable.call();
    } 
  }
}
