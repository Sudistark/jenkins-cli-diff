package hudson.model;

import jenkins.model.Jenkins;

public abstract class AbstractDescribableImpl<T extends AbstractDescribableImpl<T>> extends Object implements Describable<T> {
  public Descriptor<T> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
}
