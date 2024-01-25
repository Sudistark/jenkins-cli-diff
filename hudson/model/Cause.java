package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Cause {
  @Exported(visibility = 3)
  public abstract String getShortDescription();
  
  public void onAddedTo(@NonNull Run build) {
    if (build instanceof AbstractBuild)
      onAddedTo((AbstractBuild)build); 
  }
  
  @Deprecated
  public void onAddedTo(AbstractBuild build) {
    if (Util.isOverridden(Cause.class, getClass(), "onAddedTo", new Class[] { Run.class }))
      onAddedTo(build); 
  }
  
  public void onLoad(@NonNull Run<?, ?> build) {
    if (build instanceof AbstractBuild)
      onLoad((AbstractBuild)build); 
  }
  
  void onLoad(@NonNull Job<?, ?> job, int buildNumber) {
    Run<?, ?> build = job.getBuildByNumber(buildNumber);
    if (build != null)
      onLoad(build); 
  }
  
  @Deprecated
  public void onLoad(AbstractBuild<?, ?> build) {
    if (Util.isOverridden(Cause.class, getClass(), "onLoad", new Class[] { Run.class }))
      onLoad(build); 
  }
  
  public void print(TaskListener listener) { listener.getLogger().println(getShortDescription()); }
}
