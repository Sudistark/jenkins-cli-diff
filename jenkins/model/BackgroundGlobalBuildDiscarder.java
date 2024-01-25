package jenkins.model;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.Job;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class BackgroundGlobalBuildDiscarder extends AsyncPeriodicWork {
  private static final Logger LOGGER = Logger.getLogger(BackgroundGlobalBuildDiscarder.class.getName());
  
  public BackgroundGlobalBuildDiscarder() { super("Periodic background build discarder"); }
  
  protected void execute(TaskListener listener) throws IOException, InterruptedException {
    for (Job job : Jenkins.get().allItems(Job.class))
      processJob(listener, job); 
  }
  
  public static void processJob(TaskListener listener, Job job) {
    listener.getLogger().println("Processing " + job.getFullName());
    GlobalBuildDiscarderConfiguration.get().getConfiguredBuildDiscarders().forEach(strategy -> {
          String displayName = strategy.getDescriptor().getDisplayName();
          listener.getLogger().println("Offering " + job.getFullName() + " to " + displayName);
          if (strategy.isApplicable(job)) {
            listener.getLogger().println(job.getFullName() + " accepted by " + job.getFullName());
            try {
              strategy.apply(job);
            } catch (Exception ex) {
              listener.error("An exception occurred when executing " + displayName + ": " + ex.getMessage());
              LOGGER.log(Level.WARNING, "An exception occurred when executing " + displayName, ex);
            } 
          } 
        });
  }
  
  public long getRecurrencePeriod() { return 3600000L; }
}
