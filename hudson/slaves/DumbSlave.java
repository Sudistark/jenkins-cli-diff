package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.Slave;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

public final class DumbSlave extends Slave {
  @Deprecated
  public DumbSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy) throws Descriptor.FormException, IOException { this(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, new ArrayList()); }
  
  @Deprecated
  public DumbSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Node.Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws IOException, Descriptor.FormException { super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties); }
  
  @DataBoundConstructor
  public DumbSlave(@NonNull String name, String remoteFS, ComputerLauncher launcher) throws Descriptor.FormException, IOException { super(name, remoteFS, launcher); }
}
