package jenkins.model;

import hudson.model.Job;
import java.io.IOException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class SimpleGlobalBuildDiscarderStrategy extends GlobalBuildDiscarderStrategy {
  private BuildDiscarder discarder;
  
  @DataBoundConstructor
  public SimpleGlobalBuildDiscarderStrategy(BuildDiscarder discarder) { this.discarder = discarder; }
  
  public BuildDiscarder getDiscarder() { return this.discarder; }
  
  public boolean isApplicable(Job<?, ?> job) { return true; }
  
  public void apply(Job<?, ?> job) throws IOException, InterruptedException {
    if (this.discarder != null)
      this.discarder.perform(job); 
  }
}
