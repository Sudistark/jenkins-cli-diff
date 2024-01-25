package jenkins.model;

import hudson.model.Computer;
import hudson.model.LoadStatistics;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.SubTask;

public class UnlabeledLoadStatistics extends LoadStatistics {
  private final Iterable<Node> nodes = new UnlabeledNodesIterable();
  
  UnlabeledLoadStatistics() { super(0, 0); }
  
  public int computeIdleExecutors() {
    int r = 0;
    for (Computer c : Jenkins.get().getComputers()) {
      Node node = c.getNode();
      if (node != null && node.getMode() == Node.Mode.NORMAL && (c.isOnline() || c.isConnecting()) && c.isAcceptingTasks())
        r += c.countIdle(); 
    } 
    return r;
  }
  
  public int computeTotalExecutors() {
    int r = 0;
    for (Computer c : Jenkins.get().getComputers()) {
      Node node = c.getNode();
      if (node != null && node.getMode() == Node.Mode.NORMAL && c.isOnline())
        r += c.countExecutors(); 
    } 
    return r;
  }
  
  public int computeQueueLength() { return Jenkins.get().getQueue().strictCountBuildableItemsFor(null); }
  
  protected Iterable<Node> getNodes() { return this.nodes; }
  
  protected boolean matches(Queue.Item item, SubTask subTask) { return (item.getAssignedLabelFor(subTask) == null); }
}
