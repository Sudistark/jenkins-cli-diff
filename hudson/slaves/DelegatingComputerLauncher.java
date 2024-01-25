package hudson.slaves;

import hudson.model.TaskListener;
import java.io.IOException;

public abstract class DelegatingComputerLauncher extends ComputerLauncher {
  protected ComputerLauncher launcher;
  
  protected DelegatingComputerLauncher(ComputerLauncher launcher) { this.launcher = launcher; }
  
  public ComputerLauncher getLauncher() { return this.launcher; }
  
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { getLauncher().launch(computer, listener); }
  
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { getLauncher().afterDisconnect(computer, listener); }
  
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { getLauncher().beforeDisconnect(computer, listener); }
}
