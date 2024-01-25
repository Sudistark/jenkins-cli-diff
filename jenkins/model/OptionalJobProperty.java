package jenkins.model;

import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;

public abstract class OptionalJobProperty<J extends Job<?, ?>> extends JobProperty<J> {
  public OptionalJobPropertyDescriptor getDescriptor() { return (OptionalJobPropertyDescriptor)super.getDescriptor(); }
}
