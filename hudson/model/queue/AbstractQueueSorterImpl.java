package hudson.model.queue;

import hudson.RestrictedSince;
import hudson.model.Queue;
import java.util.Comparator;
import java.util.List;
import org.kohsuke.accmod.Restricted;

public abstract class AbstractQueueSorterImpl extends QueueSorter implements Comparator<Queue.BuildableItem> {
  public void sortBuildableItems(List<Queue.BuildableItem> buildables) { buildables.sort(this); }
  
  public int compare(Queue.BuildableItem lhs, Queue.BuildableItem rhs) { return Long.compare(lhs.buildableStartMilliseconds, rhs.buildableStartMilliseconds); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.211")
  protected static int compare(long a, long b) { return Long.compare(a, b); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.211")
  protected static int compare(int a, int b) { return Integer.compare(a, b); }
}
