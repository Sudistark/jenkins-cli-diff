package hudson.model.queue;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Queue;

public abstract class QueueListener implements ExtensionPoint {
  public void onEnterWaiting(Queue.WaitingItem wi) {}
  
  public void onLeaveWaiting(Queue.WaitingItem wi) {}
  
  public void onEnterBlocked(Queue.BlockedItem bi) {}
  
  public void onLeaveBlocked(Queue.BlockedItem bi) {}
  
  public void onEnterBuildable(Queue.BuildableItem bi) {}
  
  public void onLeaveBuildable(Queue.BuildableItem bi) {}
  
  public void onLeft(Queue.LeftItem li) {}
  
  public static ExtensionList<QueueListener> all() { return ExtensionList.lookup(QueueListener.class); }
}
