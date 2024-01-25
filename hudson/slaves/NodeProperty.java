package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Environment;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ReconfigurableDescribable;
import hudson.model.TaskListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.tools.PropertyDescriptor;
import java.io.IOException;
import java.util.List;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public abstract class NodeProperty<N extends Node> extends Object implements ReconfigurableDescribable<NodeProperty<?>>, ExtensionPoint {
  protected N node;
  
  protected void setNode(N node) { this.node = node; }
  
  public NodePropertyDescriptor getDescriptor() { return (NodePropertyDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @Deprecated
  public CauseOfBlockage canTake(Queue.Task task) { return null; }
  
  public CauseOfBlockage canTake(Queue.BuildableItem item) { return canTake(item.task); }
  
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException { return new Object(this); }
  
  public void buildEnvVars(@NonNull EnvVars env, @NonNull TaskListener listener) throws IOException, InterruptedException {}
  
  public NodeProperty<?> reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException { return (form == null) ? null : (NodeProperty)getDescriptor().newInstance(req, form); }
  
  public static DescriptorExtensionList<NodeProperty<?>, NodePropertyDescriptor> all() { return Jenkins.get().getDescriptorList(NodeProperty.class); }
  
  public static List<NodePropertyDescriptor> for_(Node node) { return PropertyDescriptor.for_(all(), node); }
}
