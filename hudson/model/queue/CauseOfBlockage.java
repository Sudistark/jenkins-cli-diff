package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import java.util.Objects;
import org.jvnet.localizer.Localizable;

public abstract class CauseOfBlockage {
  public abstract String getShortDescription();
  
  public void print(TaskListener listener) { listener.getLogger().println(getShortDescription()); }
  
  public static CauseOfBlockage fromMessage(@NonNull Localizable l) {
    Objects.requireNonNull(l);
    return new Object(l);
  }
  
  public String toString() { return getShortDescription(); }
  
  public static CauseOfBlockage createNeedsMoreExecutor(Localizable l) { return new NeedsMoreExecutorImpl(l); }
}
