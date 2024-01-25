package jenkins.model;

import hudson.model.Job;
import java.io.IOException;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class JobGlobalBuildDiscarderStrategy extends GlobalBuildDiscarderStrategy {
  public boolean isApplicable(Job<?, ?> job) { return (job.getBuildDiscarder() != null); }
  
  public void apply(Job<?, ?> job) throws IOException, InterruptedException { job.logRotate(); }
}
