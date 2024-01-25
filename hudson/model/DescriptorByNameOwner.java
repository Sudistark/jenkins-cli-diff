package hudson.model;

import jenkins.model.Jenkins;

public interface DescriptorByNameOwner extends ModelObject {
  default Descriptor getDescriptorByName(String id) { return Jenkins.get().getDescriptorByName(id); }
}
