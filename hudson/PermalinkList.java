package hudson;

import hudson.model.PermalinkProjectAction;
import hudson.util.EditDistance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PermalinkList extends ArrayList<PermalinkProjectAction.Permalink> {
  public PermalinkList(Collection<? extends PermalinkProjectAction.Permalink> c) { super(c); }
  
  public PermalinkList() {}
  
  public PermalinkProjectAction.Permalink get(String id) {
    for (PermalinkProjectAction.Permalink p : this) {
      if (p.getId().equals(id))
        return p; 
    } 
    return null;
  }
  
  public PermalinkProjectAction.Permalink findNearest(String id) {
    List<String> ids = new ArrayList<String>();
    for (PermalinkProjectAction.Permalink p : this)
      ids.add(p.getId()); 
    String nearest = EditDistance.findNearest(id, ids);
    if (nearest == null)
      return null; 
    return get(nearest);
  }
}
