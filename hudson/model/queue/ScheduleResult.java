package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Queue;

public abstract class ScheduleResult {
  public boolean isCreated() { return false; }
  
  public boolean isRefused() { return false; }
  
  @CheckForNull
  public Queue.Item getItem() { return null; }
  
  @CheckForNull
  public Queue.WaitingItem getCreateItem() { return null; }
  
  public final boolean isAccepted() { return !isRefused(); }
  
  public static Created created(Queue.WaitingItem i) { return new Created(i); }
  
  public static Existing existing(Queue.Item i) { return new Existing(i); }
  
  public static Refused refused() { return new Refused(); }
}
