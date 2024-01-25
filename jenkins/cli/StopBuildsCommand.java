package jenkins.cli;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.model.Executor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class StopBuildsCommand extends CLICommand {
  @Argument(usage = "Name of the job(s) to stop", required = true, multiValued = true)
  private List<String> jobNames;
  
  private boolean isAnyBuildStopped;
  
  public String getShortDescription() { return "Stop all running builds for job(s)"; }
  
  protected int run() throws Exception {
    Jenkins jenkins = Jenkins.get();
    Set<String> names = new LinkedHashSet<String>(this.jobNames);
    List<Job> jobsToStop = new ArrayList<Job>();
    for (String jobName : names) {
      Item item = jenkins.getItemByFullName(jobName);
      if (item instanceof Job) {
        jobsToStop.add((Job)item);
        continue;
      } 
      throw new IllegalArgumentException("Job not found: '" + jobName + "'");
    } 
    for (Job job : jobsToStop)
      stopJobBuilds(job); 
    if (!this.isAnyBuildStopped)
      this.stdout.println("No builds stopped"); 
    return 0;
  }
  
  private void stopJobBuilds(Job job) {
    Run lastBuild = job.getLastBuild();
    String jobName = job.getFullDisplayName();
    if (lastBuild != null) {
      if (lastBuild.isBuilding())
        stopBuild(lastBuild, jobName); 
      checkAndStopPreviousBuilds(lastBuild, jobName);
    } 
  }
  
  private void stopBuild(Run build, String jobName) {
    String buildName = build.getDisplayName();
    Executor executor = build.getExecutor();
    if (executor != null) {
      try {
        executor.doStop();
        this.isAnyBuildStopped = true;
        this.stdout.printf("Build '%s' stopped for job '%s'%n", new Object[] { buildName, jobName });
      } catch (RuntimeException e) {
        this.stdout.printf("Exception occurred while trying to stop build '%s' for job '%s'. ", new Object[] { buildName, jobName });
        this.stdout.printf("Exception class: %s, message: %s%n", new Object[] { e.getClass().getSimpleName(), e.getMessage() });
      } 
    } else {
      this.stdout.printf("Build '%s' in job '%s' not stopped%n", new Object[] { buildName, jobName });
    } 
  }
  
  private void checkAndStopPreviousBuilds(Run lastBuild, String jobName) {
    Run build = lastBuild.getPreviousBuildInProgress();
    while (build != null) {
      stopBuild(build, jobName);
      build = build.getPreviousBuildInProgress();
    } 
  }
}
