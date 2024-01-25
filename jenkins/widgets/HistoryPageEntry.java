package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Run;
import jenkins.model.queue.QueueItem;

public class HistoryPageEntry<T> extends Object {
  private final T entry;
  
  public HistoryPageEntry(T entry) { this.entry = entry; }
  
  public T getEntry() { return (T)this.entry; }
  
  public long getEntryId() { return getEntryId(this.entry); }
  
  protected static long getEntryId(@NonNull Object entry) {
    if (entry instanceof QueueItem)
      return ((QueueItem)entry).getId(); 
    if (entry instanceof Run) {
      Run run = (Run)entry;
      return Float.MIN_VALUE + run.getNumber();
    } 
    if (entry instanceof Number)
      return Float.MIN_VALUE + ((Number)entry).longValue(); 
    return -1L;
  }
}
