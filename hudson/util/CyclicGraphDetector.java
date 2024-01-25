package hudson.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public abstract class CyclicGraphDetector<N> extends Object {
  private final Set<N> visited = new HashSet();
  
  private final Set<N> visiting = new HashSet();
  
  private final Stack<N> path = new Stack();
  
  private final List<N> topologicalOrder = new ArrayList();
  
  public void run(Iterable<? extends N> allNodes) throws CycleDetectedException {
    for (N n : allNodes)
      visit(n); 
  }
  
  public List<N> getSorted() { return this.topologicalOrder; }
  
  protected abstract Iterable<? extends N> getEdges(N paramN);
  
  private void visit(N p) throws CycleDetectedException {
    if (!this.visited.add(p))
      return; 
    this.visiting.add(p);
    this.path.push(p);
    for (N q : getEdges(p)) {
      if (q == null)
        continue; 
      if (this.visiting.contains(q))
        detectedCycle(q); 
      visit(q);
    } 
    this.visiting.remove(p);
    this.path.pop();
    this.topologicalOrder.add(p);
  }
  
  private void detectedCycle(N q) throws CycleDetectedException {
    int i = this.path.indexOf(q);
    this.path.push(q);
    reactOnCycle(q, this.path.subList(i, this.path.size()));
  }
  
  protected void reactOnCycle(N q, List<N> cycle) throws CycleDetectedException { throw new CycleDetectedException(cycle); }
}
