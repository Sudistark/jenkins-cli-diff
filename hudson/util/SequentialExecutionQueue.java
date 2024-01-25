package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class SequentialExecutionQueue implements Executor {
  private final Map<Runnable, QueueEntry> entries;
  
  private ExecutorService executors;
  
  private final Set<QueueEntry> inProgress;
  
  public SequentialExecutionQueue(ExecutorService executors) {
    this.entries = new HashMap();
    this.inProgress = new HashSet();
    this.executors = executors;
  }
  
  public ExecutorService getExecutors() { return this.executors; }
  
  public void setExecutors(ExecutorService svc) {
    ExecutorService old = this.executors;
    this.executors = svc;
    old.shutdown();
  }
  
  public void execute(@NonNull Runnable item) {
    QueueEntry e = (QueueEntry)this.entries.get(item);
    if (e == null) {
      e = new QueueEntry(this, item);
      this.entries.put(item, e);
      e.submit();
    } else {
      e.queued = true;
    } 
  }
  
  public boolean isStarving(long threshold) {
    long now = System.currentTimeMillis();
    for (QueueEntry e : this.entries.values()) {
      if (now - e.submissionTime > threshold)
        return true; 
    } 
    return false;
  }
  
  public Set<Runnable> getInProgress() {
    Set<Runnable> items = new HashSet<Runnable>();
    for (QueueEntry entry : this.inProgress)
      items.add(entry.item); 
    return items;
  }
}
