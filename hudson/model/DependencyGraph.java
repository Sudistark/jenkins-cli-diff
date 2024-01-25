package hudson.model;

import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import jenkins.model.DependencyDeclarer;
import jenkins.model.Jenkins;
import jenkins.util.DirectedGraph;

public class DependencyGraph extends Object implements Comparator<AbstractProject> {
  private Map<AbstractProject, List<DependencyGroup>> forward = new HashMap();
  
  private Map<AbstractProject, List<DependencyGroup>> backward = new HashMap();
  
  private Map<Class<?>, Object> computationalData;
  
  private boolean built;
  
  private Comparator<AbstractProject<?, ?>> topologicalOrder;
  
  private List<AbstractProject<?, ?>> topologicallySorted;
  
  public void build() {
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      this.computationalData = new HashMap();
      for (AbstractProject p : Jenkins.get().allItems(AbstractProject.class))
        p.buildDependencyGraph(this); 
      this.forward = finalize(this.forward);
      this.backward = finalize(this.backward);
      topologicalDagSort();
      this.computationalData = null;
      this.built = true;
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private void topologicalDagSort() {
    Object object = new Object(this);
    List<DirectedGraph.SCC<AbstractProject>> sccs = object.getStronglyConnectedComponents();
    Map<AbstractProject, Integer> topoOrder = new HashMap<AbstractProject, Integer>();
    this.topologicallySorted = new ArrayList();
    int idx = 0;
    for (DirectedGraph.SCC<AbstractProject> scc : sccs) {
      for (AbstractProject n : scc) {
        topoOrder.put(n, Integer.valueOf(idx++));
        this.topologicallySorted.add(n);
      } 
    } 
    Objects.requireNonNull(topoOrder);
    this.topologicalOrder = Comparator.comparingInt(topoOrder::get);
    this.topologicallySorted = Collections.unmodifiableList(this.topologicallySorted);
  }
  
  private DependencyGraph(boolean dummy) {
    this.forward = this.backward = Collections.emptyMap();
    topologicalDagSort();
    this.built = true;
  }
  
  public <T> void putComputationalData(Class<T> key, T value) { this.computationalData.put(key, value); }
  
  public <T> T getComputationalData(Class<T> key) { return (T)this.computationalData.get(key); }
  
  public List<AbstractProject> getDownstream(AbstractProject p) { return get(this.forward, p, false); }
  
  public List<AbstractProject> getUpstream(AbstractProject p) { return get(this.backward, p, true); }
  
  private List<AbstractProject> get(Map<AbstractProject, List<DependencyGroup>> map, AbstractProject src, boolean up) {
    List<DependencyGroup> v = (List)map.get(src);
    if (v == null)
      return Collections.emptyList(); 
    List<AbstractProject> result = new ArrayList<AbstractProject>(v.size());
    for (DependencyGroup d : v)
      result.add(up ? d.getUpstreamProject() : d.getDownstreamProject()); 
    return result;
  }
  
  public List<Dependency> getDownstreamDependencies(AbstractProject p) { return get(this.forward, p); }
  
  public List<Dependency> getUpstreamDependencies(AbstractProject p) { return get(this.backward, p); }
  
  private List<Dependency> get(Map<AbstractProject, List<DependencyGroup>> map, AbstractProject src) {
    List<DependencyGroup> v = (List)map.get(src);
    if (v == null)
      return Collections.emptyList(); 
    List<Dependency> builder = new ArrayList<Dependency>();
    for (DependencyGroup dependencyGroup : v)
      builder.addAll(dependencyGroup.getGroup()); 
    return Collections.unmodifiableList(builder);
  }
  
  @Deprecated
  public void addDependency(AbstractProject upstream, AbstractProject downstream) { addDependency(new Dependency(upstream, downstream)); }
  
  public void addDependency(Dependency dep) {
    if (this.built)
      throw new IllegalStateException(); 
    add(this.forward, dep.getUpstreamProject(), dep);
    add(this.backward, dep.getDownstreamProject(), dep);
  }
  
  @Deprecated
  public void addDependency(AbstractProject upstream, Collection<? extends AbstractProject> downstream) {
    for (AbstractProject p : downstream)
      addDependency(upstream, p); 
  }
  
  @Deprecated
  public void addDependency(Collection<? extends AbstractProject> upstream, AbstractProject downstream) {
    for (AbstractProject p : upstream)
      addDependency(p, downstream); 
  }
  
  public void addDependencyDeclarers(AbstractProject upstream, Collection<?> possibleDependecyDeclarers) {
    for (Object o : possibleDependecyDeclarers) {
      if (o instanceof DependencyDeclarer) {
        DependencyDeclarer dd = (DependencyDeclarer)o;
        dd.buildDependencyGraph(upstream, this);
      } 
    } 
  }
  
  public boolean hasIndirectDependencies(AbstractProject src, AbstractProject dst) {
    Set<AbstractProject> visited = new HashSet<AbstractProject>();
    Stack<AbstractProject> queue = new Stack<AbstractProject>();
    queue.addAll(getDownstream(src));
    queue.remove(dst);
    while (!queue.isEmpty()) {
      AbstractProject p = (AbstractProject)queue.pop();
      if (p == dst)
        return true; 
      if (visited.add(p))
        queue.addAll(getDownstream(p)); 
    } 
    return false;
  }
  
  public Set<AbstractProject> getTransitiveUpstream(AbstractProject src) { return getTransitive(this.backward, src, true); }
  
  public Set<AbstractProject> getTransitiveDownstream(AbstractProject src) { return getTransitive(this.forward, src, false); }
  
  private Set<AbstractProject> getTransitive(Map<AbstractProject, List<DependencyGroup>> direction, AbstractProject src, boolean up) {
    Set<AbstractProject> visited = new HashSet<AbstractProject>();
    Stack<AbstractProject> queue = new Stack<AbstractProject>();
    queue.add(src);
    while (!queue.isEmpty()) {
      AbstractProject p = (AbstractProject)queue.pop();
      for (AbstractProject child : get(direction, p, up)) {
        if (visited.add(child))
          queue.add(child); 
      } 
    } 
    return visited;
  }
  
  private void add(Map<AbstractProject, List<DependencyGroup>> map, AbstractProject key, Dependency dep) {
    List<DependencyGroup> set = (List)map.computeIfAbsent(key, k -> new ArrayList());
    for (DependencyGroup d : set) {
      if (d.getUpstreamProject() == dep.getUpstreamProject() && d.getDownstreamProject() == dep.getDownstreamProject()) {
        d.add(dep);
        return;
      } 
    } 
    set.add(new DependencyGroup(dep));
  }
  
  private Map<AbstractProject, List<DependencyGroup>> finalize(Map<AbstractProject, List<DependencyGroup>> m) {
    for (Map.Entry<AbstractProject, List<DependencyGroup>> e : m.entrySet()) {
      ((List)e.getValue()).sort(NAME_COMPARATOR);
      e.setValue(Collections.unmodifiableList((List)e.getValue()));
    } 
    return Collections.unmodifiableMap(m);
  }
  
  private static final Comparator<DependencyGroup> NAME_COMPARATOR = new Object();
  
  public static final DependencyGraph EMPTY = new DependencyGraph(false);
  
  public int compare(AbstractProject o1, AbstractProject o2) { return this.topologicalOrder.compare(o1, o2); }
  
  public List<AbstractProject<?, ?>> getTopologicallySorted() { return this.topologicallySorted; }
  
  public DependencyGraph() {}
}
