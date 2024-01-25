package hudson.slaves;

import hudson.model.Node;
import hudson.tools.PropertyDescriptor;
import jenkins.model.Jenkins;

public abstract class NodePropertyDescriptor extends PropertyDescriptor<NodeProperty<?>, Node> {
  protected NodePropertyDescriptor(Class<? extends NodeProperty<?>> clazz) { super(clazz); }
  
  protected NodePropertyDescriptor() {}
  
  public boolean isApplicableAsGlobal() { return isApplicable(Jenkins.get().getClass()); }
}
