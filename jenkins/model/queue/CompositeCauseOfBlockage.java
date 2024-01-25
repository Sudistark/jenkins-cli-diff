package jenkins.model.queue;

import hudson.model.TaskListener;
import hudson.model.queue.CauseOfBlockage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CompositeCauseOfBlockage extends CauseOfBlockage {
  public final Map<String, CauseOfBlockage> uniqueReasons = new TreeMap();
  
  public CompositeCauseOfBlockage(List<CauseOfBlockage> delegates) {
    for (CauseOfBlockage delegate : delegates)
      this.uniqueReasons.put(delegate.getShortDescription(), delegate); 
  }
  
  public String getShortDescription() { return String.join("; ", this.uniqueReasons.keySet()); }
  
  public void print(TaskListener listener) {
    for (CauseOfBlockage delegate : this.uniqueReasons.values())
      delegate.print(listener); 
  }
}
