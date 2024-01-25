package hudson.model;

import hudson.model.queue.FoldableAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class CauseAction implements FoldableAction, RunAction2 {
  @Deprecated
  private Cause cause;
  
  @Deprecated
  private List<Cause> causes;
  
  private Map<Cause, Integer> causeBag;
  
  public CauseAction(Cause c) {
    this.causeBag = new LinkedHashMap();
    this.causeBag.put(c, Integer.valueOf(1));
  }
  
  private void addCause(Cause c) {
    synchronized (this.causeBag) {
      Integer cnt = (Integer)this.causeBag.get(c);
      this.causeBag.put(c, Integer.valueOf((cnt == null) ? 1 : (cnt.intValue() + 1)));
    } 
  }
  
  private void addCauses(Collection<? extends Cause> causes) {
    for (Cause cause : causes)
      addCause(cause); 
  }
  
  public CauseAction(Cause... c) { this(Arrays.asList(c)); }
  
  public CauseAction(Collection<? extends Cause> causes) {
    this.causeBag = new LinkedHashMap();
    addCauses(causes);
  }
  
  public CauseAction(CauseAction ca) {
    this.causeBag = new LinkedHashMap();
    addCauses(ca.getCauses());
  }
  
  @Exported(visibility = 2)
  public List<Cause> getCauses() {
    List<Cause> r = new ArrayList<Cause>();
    for (Map.Entry<Cause, Integer> entry : this.causeBag.entrySet())
      r.addAll(Collections.nCopies(((Integer)entry.getValue()).intValue(), (Cause)entry.getKey())); 
    return Collections.unmodifiableList(r);
  }
  
  public <T extends Cause> T findCause(Class<T> type) {
    for (Cause c : this.causeBag.keySet()) {
      if (type.isInstance(c))
        return (T)(Cause)type.cast(c); 
    } 
    return null;
  }
  
  public String getDisplayName() { return "Cause"; }
  
  public String getIconFileName() { return null; }
  
  public String getUrlName() { return "cause"; }
  
  public Map<Cause, Integer> getCauseCounts() { return Collections.unmodifiableMap(this.causeBag); }
  
  @Deprecated
  public String getShortDescription() {
    if (this.causeBag.isEmpty())
      return "N/A"; 
    return ((Cause)this.causeBag.keySet().iterator().next()).getShortDescription();
  }
  
  public void onLoad(Run<?, ?> owner) {
    for (Cause c : this.causeBag.keySet()) {
      if (c != null)
        c.onLoad(owner); 
    } 
  }
  
  public void onAttached(Run<?, ?> owner) {
    for (Cause c : this.causeBag.keySet()) {
      if (c != null)
        c.onAddedTo(owner); 
    } 
  }
  
  public void foldIntoExisting(Queue.Item item, Queue.Task owner, List<Action> otherActions) {
    CauseAction existing = (CauseAction)item.getAction(CauseAction.class);
    if (existing != null) {
      existing.addCauses(getCauses());
      return;
    } 
    item.addAction(new CauseAction(this));
  }
}
