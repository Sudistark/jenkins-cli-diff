package hudson.model.queue;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Queue;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class QueueSorter implements ExtensionPoint {
  public static final Comparator<Queue.BlockedItem> DEFAULT_BLOCKED_ITEM_COMPARATOR = Comparator.comparingLong(Queue.Item::getInQueueSince);
  
  public abstract void sortBuildableItems(List<Queue.BuildableItem> paramList);
  
  public void sortBlockedItems(List<Queue.BlockedItem> blockedItems) { blockedItems.sort(DEFAULT_BLOCKED_ITEM_COMPARATOR); }
  
  public static ExtensionList<QueueSorter> all() { return ExtensionList.lookup(QueueSorter.class); }
  
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void installDefaultQueueSorter() {
    all = all();
    if (all.isEmpty())
      return; 
    Queue q = Jenkins.get().getQueue();
    if (q.getSorter() != null)
      return; 
    q.setSorter((QueueSorter)all.get(0));
    if (all.size() > 1)
      LOGGER.warning("Multiple QueueSorters are registered. Only the first one is used and the rest are ignored: " + all); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(QueueSorter.class.getName());
}
