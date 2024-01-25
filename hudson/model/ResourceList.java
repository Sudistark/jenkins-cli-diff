package hudson.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class ResourceList {
  private static final Logger LOGGER = Logger.getLogger(ResourceList.class.getName());
  
  private final Set<Resource> all = new HashSet();
  
  private final Map<Resource, Integer> write = new HashMap();
  
  private static final Integer MAX_INT = Integer.valueOf(2147483647);
  
  public static ResourceList union(ResourceList... lists) { return union(Arrays.asList(lists)); }
  
  public static ResourceList union(Collection<ResourceList> lists) {
    switch (lists.size()) {
      case 0:
        return EMPTY;
      case 1:
        return (ResourceList)lists.iterator().next();
    } 
    ResourceList r = new ResourceList();
    for (ResourceList l : lists) {
      r.all.addAll(l.all);
      for (Map.Entry<Resource, Integer> e : l.write.entrySet())
        r.write.put((Resource)e.getKey(), Integer.valueOf(unbox((Integer)r.write.get(e.getKey())) + ((Integer)e.getValue()).intValue())); 
    } 
    return r;
  }
  
  public ResourceList r(Resource r) {
    this.all.add(r);
    return this;
  }
  
  public ResourceList w(Resource r) {
    this.all.add(r);
    this.write.put(r, Integer.valueOf(unbox((Integer)this.write.get(r)) + 1));
    return this;
  }
  
  public boolean isCollidingWith(ResourceList that) { return (getConflict(that) != null); }
  
  public Resource getConflict(ResourceList that) {
    Resource r = _getConflict(this, that);
    if (r != null)
      return r; 
    return _getConflict(that, this);
  }
  
  private Resource _getConflict(ResourceList lhs, ResourceList rhs) {
    for (Map.Entry<Resource, Integer> r : lhs.write.entrySet()) {
      for (Resource l : rhs.all) {
        Integer v = (Integer)rhs.write.get(l);
        if (v != null) {
          v = Integer.valueOf(v.intValue() + ((Integer)r.getValue()).intValue());
        } else {
          v = MAX_INT;
        } 
        if (((Resource)r.getKey()).isCollidingWith(l, unbox(v))) {
          LOGGER.info("Collision with " + r + " and " + l);
          return (Resource)r.getKey();
        } 
      } 
    } 
    return null;
  }
  
  public String toString() {
    Map<Resource, String> m = new HashMap<Resource, String>();
    for (Resource r : this.all)
      m.put(r, "R"); 
    for (Map.Entry<Resource, Integer> e : this.write.entrySet())
      m.put((Resource)e.getKey(), "W" + e.getValue()); 
    return m.toString();
  }
  
  private static int unbox(Integer x) { return (x == null) ? 0 : x.intValue(); }
  
  public static final ResourceList EMPTY = new ResourceList();
}
