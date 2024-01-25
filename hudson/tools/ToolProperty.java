package hudson.tools;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ToolProperty<T extends ToolInstallation> extends Object implements Describable<ToolProperty<?>>, ExtensionPoint {
  protected T tool;
  
  protected void setTool(T tool) { this.tool = tool; }
  
  public ToolPropertyDescriptor getDescriptor() { return (ToolPropertyDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<ToolProperty<?>, ToolPropertyDescriptor> all() { return Jenkins.get().getDescriptorList(ToolProperty.class); }
  
  public abstract Class<T> type();
}
