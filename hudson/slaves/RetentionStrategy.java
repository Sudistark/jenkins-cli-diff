package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Queue;
import hudson.util.DescriptorList;
import jenkins.model.Jenkins;
import net.jcip.annotations.GuardedBy;

public abstract class RetentionStrategy<T extends Computer> extends AbstractDescribableImpl<RetentionStrategy<?>> implements ExtensionPoint {
  public boolean isManualLaunchAllowed(T c) { return true; }
  
  public boolean isAcceptingTasks(T c) { return true; }
  
  public void start(@NonNull T c) { Queue.withLock(() -> check(c)); }
  
  public static DescriptorExtensionList<RetentionStrategy<?>, Descriptor<RetentionStrategy<?>>> all() { return Jenkins.get().getDescriptorList(RetentionStrategy.class); }
  
  @Deprecated
  public static final DescriptorList<RetentionStrategy<?>> LIST = new DescriptorList(RetentionStrategy.class);
  
  public static final RetentionStrategy<Computer> NOOP = new NoOp();
  
  public static final Always INSTANCE = new Always();
  
  @GuardedBy("hudson.model.Queue.lock")
  public abstract long check(@NonNull T paramT);
}
