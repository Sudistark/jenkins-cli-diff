package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Node;
import hudson.model.Queue;

public abstract class QueueTaskDispatcher implements ExtensionPoint {
  @Deprecated
  @CheckForNull
  public CauseOfBlockage canTake(Node node, Queue.Task task) { return null; }
  
  @CheckForNull
  public CauseOfBlockage canTake(Node node, Queue.BuildableItem item) { return canTake(node, item.task); }
  
  @CheckForNull
  public CauseOfBlockage canRun(Queue.Item item) { return null; }
  
  public static ExtensionList<QueueTaskDispatcher> all() { return ExtensionList.lookup(QueueTaskDispatcher.class); }
}
