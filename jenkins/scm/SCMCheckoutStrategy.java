package jenkins.scm;

import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import java.io.IOException;

public abstract class SCMCheckoutStrategy extends AbstractDescribableImpl<SCMCheckoutStrategy> implements ExtensionPoint {
  public void preCheckout(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    AbstractProject<?, ?> project = build.getProject();
    if (project instanceof BuildableItemWithBuildWrappers) {
      BuildableItemWithBuildWrappers biwbw = (BuildableItemWithBuildWrappers)project;
      for (BuildWrapper bw : biwbw.getBuildWrappersList())
        bw.preCheckout(build, launcher, listener); 
    } 
  }
  
  public void checkout(AbstractBuild.AbstractBuildExecution execution) throws IOException, InterruptedException { execution.defaultCheckout(); }
  
  public SCMCheckoutStrategyDescriptor getDescriptor() { return (SCMCheckoutStrategyDescriptor)super.getDescriptor(); }
}
