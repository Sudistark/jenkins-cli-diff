package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.widgets.Widget;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.queue.QueueItem;

public class BuildQueueWidget extends Widget {
  @NonNull
  private String ownerUrl;
  
  @NonNull
  private List<QueueItem> queueItems;
  
  private boolean filtered;
  
  public BuildQueueWidget(@NonNull String ownerUrl, @NonNull List<QueueItem> queueItems) { this(ownerUrl, queueItems, false); }
  
  public BuildQueueWidget(@NonNull String ownerUrl, @NonNull List<QueueItem> queueItems, boolean filtered) {
    this.ownerUrl = ownerUrl;
    this.queueItems = new ArrayList(queueItems);
    this.filtered = filtered;
  }
  
  public String getOwnerUrl() { return this.ownerUrl; }
  
  @NonNull
  public List<QueueItem> getQueueItems() { return this.queueItems; }
  
  public boolean isFiltered() { return this.filtered; }
}
