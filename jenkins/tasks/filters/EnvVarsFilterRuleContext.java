package jenkins.tasks.filters;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.model.TaskListener;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public class EnvVarsFilterRuleContext {
  private final Launcher launcher;
  
  private final TaskListener taskListener;
  
  public EnvVarsFilterRuleContext(@NonNull Launcher launcher, @NonNull TaskListener taskListener) {
    this.launcher = launcher;
    this.taskListener = taskListener;
  }
  
  public Launcher getLauncher() { return this.launcher; }
  
  public TaskListener getTaskListener() { return this.taskListener; }
}
