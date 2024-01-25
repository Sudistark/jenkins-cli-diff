package hudson.widgets;

import hudson.model.Queue;
import java.util.LinkedList;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.model.queue.QueueItem;
import jenkins.widgets.HistoryPageFilter;

public class BuildHistoryWidget<T> extends HistoryWidget<Queue.Task, T> {
  public BuildHistoryWidget(Queue.Task owner, Iterable<T> baseList, HistoryWidget.Adapter<? super T> adapter) { super(owner, baseList, adapter); }
  
  public QueueItem getQueuedItem() { return Jenkins.get().getQueue().getItem((Queue.Task)this.owner); }
  
  public List<QueueItem> getQueuedItems() {
    LinkedList<QueueItem> list = new LinkedList<QueueItem>();
    for (Queue.Item item1 : Jenkins.get().getQueue().getItems()) {
      if (item1.getTask() == this.owner)
        list.addFirst(item1); 
    } 
    return list;
  }
  
  public HistoryPageFilter getHistoryPageFilter() {
    HistoryPageFilter<T> historyPageFilter = newPageFilter();
    historyPageFilter.add(this.baseList, getQueuedItems());
    historyPageFilter.widget = this;
    return updateFirstTransientBuildKey(historyPageFilter);
  }
}
