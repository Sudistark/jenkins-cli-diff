package jenkins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DirectedGraph<N> extends Object {
  protected abstract Collection<N> nodes();
  
  protected abstract Collection<N> forward(N paramN);
  
  public List<SCC<N>> getStronglyConnectedComponents() {
    Map<N, Node> nodes = new HashMap<N, Node>();
    for (N n : nodes())
      nodes.put(n, new Node(this, n)); 
    List<SCC<N>> sccs = new ArrayList<SCC<N>>();
    (new Tarjan(this, nodes, sccs)).traverse();
    Collections.reverse(sccs);
    return sccs;
  }
}
