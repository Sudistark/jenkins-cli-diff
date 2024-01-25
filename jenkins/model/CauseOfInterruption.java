package jenkins.model;

import hudson.model.TaskListener;
import java.io.Serializable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class CauseOfInterruption implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @Exported
  public abstract String getShortDescription();
  
  public void print(TaskListener listener) { listener.getLogger().println(getShortDescription()); }
}
