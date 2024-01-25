package jenkins.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.jcip.annotations.GuardedBy;

@Extension
public class ItemDeletion extends Queue.QueueDecisionHandler {
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  
  @GuardedBy("lock")
  private final Set<Item> registrations = new HashSet();
  
  @GuardedBy("lock")
  private boolean _contains(@NonNull Item item) {
    if (this.registrations.isEmpty())
      return false; 
    while (item != null) {
      if (this.registrations.contains(item))
        return true; 
      if (item.getParent() instanceof Item)
        item = (Item)item.getParent(); 
    } 
    return false;
  }
  
  public static boolean contains(@NonNull Item item) {
    instance = instance();
    if (instance == null)
      return false; 
    instance.lock.readLock().lock();
    try {
      return instance._contains(item);
    } finally {
      instance.lock.readLock().unlock();
    } 
  }
  
  public static boolean isRegistered(@NonNull Item item) {
    instance = instance();
    if (instance == null)
      return false; 
    instance.lock.readLock().lock();
    try {
      return instance.registrations.contains(item);
    } finally {
      instance.lock.readLock().unlock();
    } 
  }
  
  public static boolean register(@NonNull Item item) {
    instance = instance();
    if (instance == null)
      return false; 
    instance.lock.writeLock().lock();
    try {
      return instance.registrations.add(item);
    } finally {
      instance.lock.writeLock().unlock();
    } 
  }
  
  public static void deregister(@NonNull Item item) {
    instance = instance();
    if (instance != null) {
      instance.lock.writeLock().lock();
      try {
        instance.registrations.remove(item);
      } finally {
        instance.lock.writeLock().unlock();
      } 
    } 
  }
  
  @CheckForNull
  private static ItemDeletion instance() { return (ItemDeletion)ExtensionList.lookup(Queue.QueueDecisionHandler.class).get(ItemDeletion.class); }
  
  public boolean shouldSchedule(Queue.Task p, List<Action> actions) {
    Item item = Tasks.getItemOf(p);
    if (item != null) {
      this.lock.readLock().lock();
      try {
        return !_contains(item);
      } finally {
        this.lock.readLock().unlock();
      } 
    } 
    return true;
  }
}
