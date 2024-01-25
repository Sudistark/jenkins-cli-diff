package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class AbstractCloudSlave extends Slave {
  protected AbstractCloudSlave(@NonNull String name, String remoteFS, ComputerLauncher launcher) throws Descriptor.FormException, IOException { super(name, remoteFS, launcher); }
  
  @Deprecated
  protected AbstractCloudSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException { super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties); }
  
  @Deprecated
  protected AbstractCloudSlave(String name, String nodeDescription, String remoteFS, int numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws Descriptor.FormException, IOException { super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties); }
  
  public void terminate() throws InterruptedException, IOException {
    Computer computer = toComputer();
    if (computer != null)
      computer.recordTermination(); 
    try {
      _terminate((computer instanceof SlaveComputer) ? ((SlaveComputer)computer).getListener() : new LogTaskListener(LOGGER, Level.INFO));
    } finally {
      try {
        Jenkins.get().removeNode(this);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to remove " + this.name, e);
      } 
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(AbstractCloudSlave.class.getName());
  
  public abstract AbstractCloudComputer createComputer();
  
  protected abstract void _terminate(TaskListener paramTaskListener) throws IOException, InterruptedException;
}
