package jenkins.widgets;

import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.search.UserSearchProperty;
import hudson.util.Iterators;
import hudson.widgets.HistoryWidget;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jenkins.model.queue.QueueItem;

public class HistoryPageFilter<T> extends Object {
  private final int maxEntries;
  
  private Long newerThan;
  
  private Long olderThan;
  
  private String searchString;
  
  public final List<HistoryPageEntry<QueueItem>> queueItems;
  
  public final List<HistoryPageEntry<Run>> runs;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public boolean hasUpPage;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public boolean hasDownPage;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public long nextBuildNumber;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public HistoryWidget widget;
  
  public long newestOnPage;
  
  public long oldestOnPage;
  
  public HistoryPageFilter(int maxEntries) {
    this.queueItems = new ArrayList();
    this.runs = new ArrayList();
    this.hasUpPage = false;
    this.hasDownPage = false;
    this.newestOnPage = Float.MIN_VALUE;
    this.oldestOnPage = Float.MAX_VALUE;
    this.maxEntries = maxEntries;
  }
  
  public void setNewerThan(Long newerThan) {
    if (this.olderThan != null)
      throw new UnsupportedOperationException("Cannot set 'newerThan'. 'olderThan' already set."); 
    this.newerThan = newerThan;
  }
  
  public void setOlderThan(Long olderThan) {
    if (this.newerThan != null)
      throw new UnsupportedOperationException("Cannot set 'olderThan'. 'newerThan' already set."); 
    this.olderThan = olderThan;
  }
  
  public void setSearchString(@NonNull String searchString) { this.searchString = searchString; }
  
  public void add(@NonNull Iterable<T> runItems) { addInternal(runItems); }
  
  public void add(@NonNull Iterable<T> runItems, @NonNull List<QueueItem> queueItems) {
    sort(queueItems);
    addInternal(Iterables.concat(queueItems, runItems));
  }
  
  private <ItemT> void addInternal(@NonNull Iterable<ItemT> items) {
    if (!items.iterator().hasNext())
      return; 
    this.nextBuildNumber = getNextBuildNumber(items.iterator().next());
    if (this.newerThan == null && this.olderThan == null) {
      Iterator<ItemT> iter = items.iterator();
      while (iter.hasNext()) {
        add(iter.next());
        if (isFull())
          break; 
      } 
      this.hasDownPage = iter.hasNext();
    } else if (this.newerThan != null) {
      int toFillCount = getFillCount();
      if (toFillCount > 0) {
        LinkedList<ItemT> itemsToAdd = new LinkedList<ItemT>();
        Iterator<ItemT> iter = items.iterator();
        while (iter.hasNext()) {
          ItemT item = (ItemT)iter.next();
          if (HistoryPageEntry.getEntryId(item) > this.newerThan.longValue()) {
            itemsToAdd.addLast(item);
            if (itemsToAdd.size() > toFillCount) {
              itemsToAdd.removeFirst();
              this.hasUpPage = true;
            } 
          } 
        } 
        if (itemsToAdd.isEmpty()) {
          this.hasDownPage = true;
        } else {
          if (itemsToAdd.size() < toFillCount) {
            Iterator<ItemT> skippedIter = items.iterator();
            Iterators.skip(skippedIter, itemsToAdd.size());
            for (int i = itemsToAdd.size(); i < toFillCount && skippedIter.hasNext(); i++) {
              ItemT item = (ItemT)skippedIter.next();
              itemsToAdd.addLast(item);
            } 
          } 
          this.hasDownPage = iter.hasNext();
          for (Object item : itemsToAdd)
            add(item); 
        } 
      } 
    } else {
      Iterator<ItemT> iter = items.iterator();
      while (iter.hasNext()) {
        Object item = iter.next();
        if (HistoryPageEntry.getEntryId(item) >= this.olderThan.longValue()) {
          this.hasUpPage = true;
          continue;
        } 
        add(item);
        if (isFull()) {
          this.hasDownPage = iter.hasNext();
          break;
        } 
      } 
    } 
  }
  
  public int size() { return this.queueItems.size() + this.runs.size(); }
  
  private void sort(List<?> items) { items.sort(new Object(this)); }
  
  private long getNextBuildNumber(@NonNull Object entry) {
    if (entry instanceof QueueItem) {
      Queue.Task task = ((QueueItem)entry).getTask();
      if (task instanceof Job)
        return ((Job)task).getNextBuildNumber(); 
    } else if (entry instanceof Run) {
      return ((Run)entry).getParent().getNextBuildNumber();
    } 
    return HistoryPageEntry.getEntryId(entry) + 1L;
  }
  
  private void addQueueItem(QueueItem item) {
    HistoryPageEntry<QueueItem> entry = new HistoryPageEntry<QueueItem>(item);
    this.queueItems.add(entry);
    updateNewestOldest(entry.getEntryId());
  }
  
  private void addRun(Run run) {
    HistoryPageEntry<Run> entry = new HistoryPageEntry<Run>(run);
    if (this.runs.size() > 0 && 
      entry.getEntryId() > ((HistoryPageEntry)this.runs.get(this.runs.size() - 1)).getEntryId())
      throw new IllegalStateException("Runs were out of order"); 
    this.runs.add(entry);
    updateNewestOldest(entry.getEntryId());
  }
  
  private void updateNewestOldest(long entryId) {
    this.newestOnPage = Math.max(this.newestOnPage, entryId);
    this.oldestOnPage = Math.min(this.oldestOnPage, entryId);
  }
  
  private boolean add(Object entry) {
    if (entry instanceof QueueItem) {
      QueueItem item = (QueueItem)entry;
      if (this.searchString != null && !fitsSearchParams(item))
        return false; 
      addQueueItem(item);
      return true;
    } 
    if (entry instanceof Run) {
      Run run = (Run)entry;
      if (this.searchString != null && !fitsSearchParams(run))
        return false; 
      addRun(run);
      return true;
    } 
    return false;
  }
  
  private boolean isFull() { return (size() >= this.maxEntries); }
  
  private int getFillCount() { return Math.max(0, this.maxEntries - size()); }
  
  private boolean fitsSearchParams(@NonNull QueueItem item) {
    if (fitsSearchString(item.getDisplayName()))
      return true; 
    if (fitsSearchString(Long.valueOf(item.getId())))
      return true; 
    return false;
  }
  
  private boolean fitsSearchParams(@NonNull Run run) {
    if (this.searchString == null)
      return true; 
    if (fitsSearchString(run.getDisplayName()))
      return true; 
    if (fitsSearchString(run.getDescription()))
      return true; 
    if (fitsSearchString(Integer.valueOf(run.getNumber())))
      return true; 
    if (fitsSearchString(Long.valueOf(run.getQueueId())))
      return true; 
    if (fitsSearchString(run.getResult()))
      return true; 
    if (run instanceof AbstractBuild && fitsSearchBuildVariables((AbstractBuild)run))
      return true; 
    ParametersAction parametersAction = (ParametersAction)run.getAction(ParametersAction.class);
    if (parametersAction != null && fitsSearchBuildParameters(parametersAction))
      return true; 
    return false;
  }
  
  private boolean fitsSearchString(Object data) {
    if (this.searchString == null)
      return true; 
    if (data == null)
      return false; 
    if (data instanceof Number)
      return data.toString().equals(this.searchString); 
    if (UserSearchProperty.isCaseInsensitive())
      return data.toString().toLowerCase().contains(this.searchString.toLowerCase()); 
    return data.toString().contains(this.searchString);
  }
  
  private boolean fitsSearchBuildVariables(AbstractBuild<?, ?> runAsBuild) {
    Map<String, String> buildVariables = runAsBuild.getBuildVariables();
    Set<String> sensitiveBuildVariables = runAsBuild.getSensitiveBuildVariables();
    for (Map.Entry<String, String> param : buildVariables.entrySet()) {
      if (!sensitiveBuildVariables.contains(param.getKey()) && fitsSearchString(param.getValue()))
        return true; 
    } 
    return false;
  }
  
  private boolean fitsSearchBuildParameters(ParametersAction parametersAction) {
    List<ParameterValue> parameters = parametersAction.getParameters();
    for (ParameterValue parameter : parameters) {
      if (!parameter.isSensitive() && fitsSearchString(parameter.getValue()))
        return true; 
    } 
    return false;
  }
}
