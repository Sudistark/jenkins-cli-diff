package jenkins.model;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Job;
import hudson.model.Run;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GlobalBuildDiscarderStrategy extends AbstractDescribableImpl<GlobalBuildDiscarderStrategy> implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(GlobalBuildDiscarderStrategy.class.getName());
  
  public abstract boolean isApplicable(Job<?, ?> paramJob);
  
  public void apply(Job<? extends Job, ? extends Run> job) throws IOException, InterruptedException {
    for (Run<? extends Job, ? extends Run> run : job.getBuilds()) {
      try {
        apply(run);
      } catch (IOException ex) {
        LOGGER.log(Level.WARNING, "Failed to delete " + run.getFullDisplayName(), ex);
      } 
    } 
  }
  
  public void apply(Run<?, ?> run) throws IOException, InterruptedException {}
}
