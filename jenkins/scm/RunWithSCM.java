package jenkins.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.export.Exported;

public interface RunWithSCM<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT> & RunWithSCM<JobT, RunT>> {
  @NonNull
  List<ChangeLogSet<? extends ChangeLogSet.Entry>> getChangeSets();
  
  @CheckForNull
  Set<String> getCulpritIds();
  
  boolean shouldCalculateCulprits();
  
  @Exported
  @NonNull
  default Set<User> getCulprits() {
    if (shouldCalculateCulprits())
      return calculateCulprits(); 
    return new Object(this);
  }
  
  @NonNull
  default Set<User> calculateCulprits() {
    Set<User> r = new HashSet<User>();
    RunT p = (RunT)((Run)this).getPreviousCompletedBuild();
    if (p != null) {
      Result pr = p.getResult();
      if (pr != null && pr.isWorseThan(Result.SUCCESS))
        r.addAll(((RunWithSCM)p).getCulprits()); 
    } 
    for (ChangeLogSet<? extends ChangeLogSet.Entry> c : getChangeSets()) {
      for (ChangeLogSet.Entry e : c)
        r.add(e.getAuthor()); 
    } 
    return r;
  }
  
  default boolean hasParticipant(User user) {
    for (ChangeLogSet<? extends ChangeLogSet.Entry> c : getChangeSets()) {
      for (ChangeLogSet.Entry e : c) {
        try {
          if (e.getAuthor() == user)
            return true; 
        } catch (RuntimeException re) {
          Logger LOGGER = Logger.getLogger(RunWithSCM.class.getName());
          LOGGER.log(Level.INFO, "Failed to determine author of changelog " + e.getCommitId() + "for " + ((Run)this).getParent().getDisplayName() + ", " + ((Run)this).getDisplayName(), re);
        } 
      } 
    } 
    return false;
  }
}
