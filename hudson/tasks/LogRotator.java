package hudson.tasks;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.model.BuildDiscarder;
import jenkins.util.io.CompositeIOException;
import org.kohsuke.stapler.DataBoundConstructor;

public class LogRotator extends BuildDiscarder {
  private final int daysToKeep;
  
  private final int numToKeep;
  
  private final Integer artifactDaysToKeep;
  
  private final Integer artifactNumToKeep;
  
  @DataBoundConstructor
  public LogRotator(String daysToKeepStr, String numToKeepStr, String artifactDaysToKeepStr, String artifactNumToKeepStr) {
    this(parse(daysToKeepStr), parse(numToKeepStr), 
        parse(artifactDaysToKeepStr), parse(artifactNumToKeepStr));
  }
  
  public static int parse(String p) {
    if (p == null)
      return -1; 
    try {
      return Integer.parseInt(p);
    } catch (NumberFormatException e) {
      return -1;
    } 
  }
  
  @Deprecated
  public LogRotator(int daysToKeep, int numToKeep) { this(daysToKeep, numToKeep, -1, -1); }
  
  public LogRotator(int daysToKeep, int numToKeep, int artifactDaysToKeep, int artifactNumToKeep) {
    this.daysToKeep = daysToKeep;
    this.numToKeep = numToKeep;
    this.artifactDaysToKeep = Integer.valueOf(artifactDaysToKeep);
    this.artifactNumToKeep = Integer.valueOf(artifactNumToKeep);
  }
  
  public void perform(Job<?, ?> job) throws IOException, InterruptedException {
    Map<Run<?, ?>, Set<IOException>> exceptionMap = new HashMap<Run<?, ?>, Set<IOException>>();
    LOGGER.log(Level.FINE, "Running the log rotation for {0} with numToKeep={1} daysToKeep={2} artifactNumToKeep={3} artifactDaysToKeep={4}", new Object[] { job, Integer.valueOf(this.numToKeep), Integer.valueOf(this.daysToKeep), this.artifactNumToKeep, this.artifactDaysToKeep });
    Run lsb = job.getLastSuccessfulBuild();
    Run lstb = job.getLastStableBuild();
    if (this.numToKeep != -1) {
      RunList<? extends Run<?, ?>> builds = job.getBuilds();
      for (Run r : builds.subList(Math.min(builds.size(), this.numToKeep), builds.size())) {
        if (shouldKeepRun(r, lsb, lstb))
          continue; 
        LOGGER.log(Level.FINE, "{0} is to be removed", r);
        try {
          r.delete();
        } catch (IOException ex) {
          ((Set)exceptionMap.computeIfAbsent(r, key -> new HashSet())).add(ex);
        } 
      } 
    } 
    if (this.daysToKeep != -1) {
      Calendar cal = new GregorianCalendar();
      cal.add(6, -this.daysToKeep);
      Run r = job.getFirstBuild();
      while (r != null && 
        !tooNew(r, cal)) {
        if (!shouldKeepRun(r, lsb, lstb)) {
          LOGGER.log(Level.FINE, "{0} is to be removed", r);
          try {
            r.delete();
          } catch (IOException ex) {
            ((Set)exceptionMap.computeIfAbsent(r, key -> new HashSet())).add(ex);
          } 
        } 
        r = r.getNextBuild();
      } 
    } 
    if (this.artifactNumToKeep != null && this.artifactNumToKeep.intValue() != -1) {
      RunList<? extends Run<?, ?>> builds = job.getBuilds();
      for (Run r : builds.subList(Math.min(builds.size(), this.artifactNumToKeep.intValue()), builds.size())) {
        if (shouldKeepRun(r, lsb, lstb))
          continue; 
        LOGGER.log(Level.FINE, "{0} is to be purged of artifacts", r);
        try {
          r.deleteArtifacts();
        } catch (IOException ex) {
          ((Set)exceptionMap.computeIfAbsent(r, key -> new HashSet())).add(ex);
        } 
      } 
    } 
    if (this.artifactDaysToKeep != null && this.artifactDaysToKeep.intValue() != -1) {
      Calendar cal = new GregorianCalendar();
      cal.add(6, -this.artifactDaysToKeep.intValue());
      Run r = job.getFirstBuild();
      while (r != null && 
        !tooNew(r, cal)) {
        if (!shouldKeepRun(r, lsb, lstb)) {
          LOGGER.log(Level.FINE, "{0} is to be purged of artifacts", r);
          try {
            r.deleteArtifacts();
          } catch (IOException ex) {
            ((Set)exceptionMap.computeIfAbsent(r, key -> new HashSet())).add(ex);
          } 
        } 
        r = r.getNextBuild();
      } 
    } 
    if (!exceptionMap.isEmpty()) {
      String msg = String.format("Failed to rotate logs for [%s]", new Object[] { exceptionMap
            
            .keySet().stream().map(Object::toString).collect(Collectors.joining(", ")) });
      throw new CompositeIOException(msg, (List)exceptionMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList()));
    } 
  }
  
  private boolean shouldKeepRun(Run r, Run lsb, Run lstb) {
    if (r.isKeepLog()) {
      LOGGER.log(Level.FINER, "{0} is not to be removed or purged of artifacts because it’s marked as a keeper", r);
      return true;
    } 
    if (r == lsb) {
      LOGGER.log(Level.FINER, "{0} is not to be removed or purged of artifacts because it’s the last successful build", r);
      return true;
    } 
    if (r == lstb) {
      LOGGER.log(Level.FINER, "{0} is not to be removed or purged of artifacts because it’s the last stable build", r);
      return true;
    } 
    if (r.isBuilding()) {
      LOGGER.log(Level.FINER, "{0} is not to be removed or purged of artifacts because it’s still building", r);
      return true;
    } 
    return false;
  }
  
  private boolean tooNew(Run r, Calendar cal) {
    if (!r.getTimestamp().before(cal)) {
      LOGGER.log(Level.FINER, "{0} is not to be removed or purged of artifacts because it’s still new", r);
      return true;
    } 
    return false;
  }
  
  public int getDaysToKeep() { return this.daysToKeep; }
  
  public int getNumToKeep() { return this.numToKeep; }
  
  public int getArtifactDaysToKeep() { return unbox(this.artifactDaysToKeep); }
  
  public int getArtifactNumToKeep() { return unbox(this.artifactNumToKeep); }
  
  public String getDaysToKeepStr() { return toString(Integer.valueOf(this.daysToKeep)); }
  
  public String getNumToKeepStr() { return toString(Integer.valueOf(this.numToKeep)); }
  
  public String getArtifactDaysToKeepStr() { return toString(this.artifactDaysToKeep); }
  
  public String getArtifactNumToKeepStr() { return toString(this.artifactNumToKeep); }
  
  private int unbox(Integer i) { return (i == null) ? -1 : i.intValue(); }
  
  private String toString(Integer i) {
    if (i == null || i.intValue() == -1)
      return ""; 
    return String.valueOf(i);
  }
  
  private static final Logger LOGGER = Logger.getLogger(LogRotator.class.getName());
}
