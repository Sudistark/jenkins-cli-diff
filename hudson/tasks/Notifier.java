package hudson.tasks;

import hudson.ExtensionPoint;
import hudson.model.Descriptor;

public abstract class Notifier extends Publisher implements ExtensionPoint {
  public BuildStepDescriptor getDescriptor() { return (BuildStepDescriptor)super.getDescriptor(); }
}
