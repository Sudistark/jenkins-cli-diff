package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.queue.CauseOfBlockage;
import java.util.Collection;

public abstract class CloudProvisioningListener implements ExtensionPoint {
  @Deprecated
  public CauseOfBlockage canProvision(Cloud cloud, Label label, int numExecutors) {
    if (Util.isOverridden(CloudProvisioningListener.class, 
        getClass(), "canProvision", new Class[] { Cloud.class, Cloud.CloudState.class, int.class }))
      return canProvision(cloud, new Cloud.CloudState(label, 0), numExecutors); 
    return null;
  }
  
  public CauseOfBlockage canProvision(Cloud cloud, Cloud.CloudState state, int numExecutors) { return canProvision(cloud, state.getLabel(), numExecutors); }
  
  public void onStarted(Cloud cloud, Label label, Collection<NodeProvisioner.PlannedNode> plannedNodes) {}
  
  public void onComplete(NodeProvisioner.PlannedNode plannedNode, Node node) {}
  
  public void onCommit(@NonNull NodeProvisioner.PlannedNode plannedNode, @NonNull Node node) {}
  
  public void onFailure(NodeProvisioner.PlannedNode plannedNode, Throwable t) {}
  
  public void onRollback(@NonNull NodeProvisioner.PlannedNode plannedNode, @NonNull Node node, @NonNull Throwable t) {}
  
  public static ExtensionList<CloudProvisioningListener> all() { return ExtensionList.lookup(CloudProvisioningListener.class); }
}
