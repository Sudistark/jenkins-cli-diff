package jenkins.model;

import hudson.model.InvisibleAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class InterruptedBuildAction extends InvisibleAction {
  private final List<CauseOfInterruption> causes;
  
  public InterruptedBuildAction(Collection<? extends CauseOfInterruption> causes) { this.causes = new ArrayList(causes); }
  
  @Exported
  public List<CauseOfInterruption> getCauses() { return Collections.unmodifiableList(this.causes); }
}
