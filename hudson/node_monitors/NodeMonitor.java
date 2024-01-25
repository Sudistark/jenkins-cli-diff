package hudson.node_monitors;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.DescriptorList;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class NodeMonitor extends Object implements ExtensionPoint, Describable<NodeMonitor> {
  @Exported
  @CheckForNull
  public String getColumnCaption() { return getDescriptor().getDisplayName(); }
  
  public AbstractNodeMonitorDescriptor<?> getDescriptor() { return (AbstractNodeMonitorDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public Object data(Computer c) { return getDescriptor().get(c); }
  
  public Thread triggerUpdate() { return getDescriptor().triggerUpdate(); }
  
  public static List<NodeMonitor> getAll() { return ComputerSet.getMonitors().toList(); }
  
  public boolean isIgnored() { return this.ignored; }
  
  public void setIgnored(boolean ignored) { this.ignored = ignored; }
  
  @Deprecated
  public static final DescriptorList<NodeMonitor> LIST = new DescriptorList(NodeMonitor.class);
  
  public static DescriptorExtensionList<NodeMonitor, Descriptor<NodeMonitor>> all() { return Jenkins.get().getDescriptorList(NodeMonitor.class); }
}
