package jenkins.model;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import java.io.IOException;

public abstract class BuildDiscarder extends AbstractDescribableImpl<BuildDiscarder> implements ExtensionPoint {
  public BuildDiscarderDescriptor getDescriptor() { return (BuildDiscarderDescriptor)super.getDescriptor(); }
  
  public abstract void perform(Job<?, ?> paramJob) throws IOException, InterruptedException;
}
