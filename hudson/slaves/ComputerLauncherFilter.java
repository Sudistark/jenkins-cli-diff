package hudson.slaves;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import java.io.IOException;

public abstract class ComputerLauncherFilter extends ComputerLauncher {
  protected ComputerLauncherFilter(ComputerLauncher core) { this.core = core; }
  
  public ComputerLauncher getCore() { return this.core; }
  
  public boolean isLaunchSupported() { return this.core.isLaunchSupported(); }
  
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { this.core.launch(computer, listener); }
  
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { this.core.afterDisconnect(computer, listener); }
  
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { this.core.beforeDisconnect(computer, listener); }
  
  public Descriptor<ComputerLauncher> getDescriptor() { throw new UnsupportedOperationException(); }
}
