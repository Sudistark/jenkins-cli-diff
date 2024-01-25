package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import java.io.IOException;

public abstract class ComputerConnector extends AbstractDescribableImpl<ComputerConnector> implements ExtensionPoint {
  public ComputerConnectorDescriptor getDescriptor() { return (ComputerConnectorDescriptor)super.getDescriptor(); }
  
  public abstract ComputerLauncher launch(@NonNull String paramString, TaskListener paramTaskListener) throws IOException, InterruptedException;
}
