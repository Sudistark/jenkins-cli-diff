package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class NodeList extends ArrayList<Node> {
  private Map<String, Node> map = new HashMap();
  
  public NodeList(Collection<? extends Node> c) {
    super(c);
    for (Node node : c) {
      if (this.map.put(node.getNodeName(), node) != null)
        throw new IllegalArgumentException(node.getNodeName() + " is defined more than once"); 
    } 
  }
  
  public NodeList(Node... toCopyIn) { this(Arrays.asList(toCopyIn)); }
  
  @CheckForNull
  public Node getNode(String nodeName) { return (Node)this.map.get(nodeName); }
  
  public void add(int index, Node element) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public Node remove(int index) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean remove(Object o) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public void clear() { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean addAll(Collection<? extends Node> c) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean addAll(int index, Collection<? extends Node> c) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  protected void removeRange(int fromIndex, int toIndex) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public boolean add(Node node) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public Node set(int index, Node element) { throw new UnsupportedOperationException("unmodifiable list"); }
  
  public NodeList() {}
}
