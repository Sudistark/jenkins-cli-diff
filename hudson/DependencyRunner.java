package hudson;

import hudson.model.AbstractProject;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public class DependencyRunner implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(DependencyRunner.class.getName());
  
  ProjectRunnable runnable;
  
  List<AbstractProject> polledProjects;
  
  public DependencyRunner(ProjectRunnable runnable) {
    this.polledProjects = new ArrayList();
    this.runnable = runnable;
  }
  
  public void run() {
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      Set<AbstractProject> topLevelProjects = new HashSet<AbstractProject>();
      LOGGER.fine("assembling top level projects");
      for (AbstractProject p : Jenkins.get().allItems(AbstractProject.class)) {
        if (p.getUpstreamProjects().size() == 0) {
          LOGGER.fine("adding top level project " + p.getName());
          topLevelProjects.add(p);
          continue;
        } 
        LOGGER.fine("skipping project since not a top level project: " + p.getName());
      } 
      populate(topLevelProjects);
      for (AbstractProject p : this.polledProjects) {
        LOGGER.fine("running project in correct dependency order: " + p.getName());
        this.runnable.run(p);
      } 
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private void populate(Collection<? extends AbstractProject> projectList) {
    for (AbstractProject<?, ?> p : projectList) {
      if (this.polledProjects.contains(p)) {
        LOGGER.fine("removing project " + p.getName() + " for re-add");
        this.polledProjects.remove(p);
      } 
      LOGGER.fine("adding project " + p.getName());
      this.polledProjects.add(p);
      populate(p.getDownstreamProjects());
    } 
  }
}
