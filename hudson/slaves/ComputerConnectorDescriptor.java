package hudson.slaves;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class ComputerConnectorDescriptor extends Descriptor<ComputerConnector> {
  public static DescriptorExtensionList<ComputerConnector, ComputerConnectorDescriptor> all() { return Jenkins.get().getDescriptorList(ComputerConnector.class); }
}
