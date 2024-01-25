package jenkins.model;

import hudson.model.Job;
import org.kohsuke.stapler.DataBoundConstructor;

public class BuildDiscarderProperty extends OptionalJobProperty<Job<?, ?>> {
  private final BuildDiscarder strategy;
  
  @DataBoundConstructor
  public BuildDiscarderProperty(BuildDiscarder strategy) { this.strategy = strategy; }
  
  public BuildDiscarder getStrategy() { return this.strategy; }
}
