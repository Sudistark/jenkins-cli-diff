package jenkins.triggers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.triggers.Trigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.DependencyDeclarer;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public final class ReverseBuildTrigger extends Trigger<Job> implements DependencyDeclarer {
  private static final Logger LOGGER = Logger.getLogger(ReverseBuildTrigger.class.getName());
  
  @CheckForNull
  private String upstreamProjects;
  
  private Result threshold;
  
  @Deprecated
  public ReverseBuildTrigger(String upstreamProjects, Result threshold) {
    this(upstreamProjects);
    this.threshold = threshold;
  }
  
  @DataBoundConstructor
  public ReverseBuildTrigger(String upstreamProjects) {
    this.threshold = Result.SUCCESS;
    this.upstreamProjects = upstreamProjects;
  }
  
  public String getUpstreamProjects() { return Util.fixNull(this.upstreamProjects); }
  
  public Result getThreshold() { return this.threshold; }
  
  @DataBoundSetter
  public void setThreshold(Result r) { this.threshold = r; }
  
  private boolean shouldTrigger(Run upstreamBuild, TaskListener listener) {
    Jenkins jenkins = Jenkins.get();
    if (this.job == null)
      return false; 
    boolean downstreamVisible = false;
    boolean downstreamDiscoverable = false;
    try {
      downstreamVisible = (jenkins.getItemByFullName(((Job)this.job).getFullName()) == this.job);
    } catch (AccessDeniedException ex) {
      downstreamDiscoverable = true;
    } 
    Authentication originalAuth = Jenkins.getAuthentication2();
    Job upstream = upstreamBuild.getParent();
    Authentication auth = Tasks.getAuthenticationOf2((Queue.Task)this.job);
    Item authUpstream = null;
    try {
      ACLContext ctx = ACL.as2(auth);
      try {
        authUpstream = jenkins.getItemByFullName(upstream.getFullName());
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
    } catch (AccessDeniedException accessDeniedException) {}
    if (authUpstream != upstream) {
      if (downstreamVisible) {
        listener.getLogger().println(Messages.ReverseBuildTrigger_running_as_cannot_even_see_for_trigger_f(auth.getName(), upstream
              .getFullName(), ((Job)this.job).getFullName()));
      } else {
        LOGGER.log(Level.WARNING, "Running as {0} cannot even {1} {2} for trigger from {3}, (but cannot tell {4} that)", new Object[] { auth
              .getName(), downstreamDiscoverable ? "READ" : "DISCOVER", upstream, this.job, originalAuth.getName() });
      } 
      return false;
    } 
    Result result = upstreamBuild.getResult();
    return (result != null && result.isBetterOrEqualTo(this.threshold));
  }
  
  public void buildDependencyGraph(AbstractProject downstream, DependencyGraph graph) {
    for (AbstractProject upstream : Items.fromNameList(downstream.getParent(), getUpstreamProjects(), AbstractProject.class))
      graph.addDependency(new Object(this, upstream, downstream)); 
  }
  
  public void start(@NonNull Job project, boolean newInstance) {
    super.start(project, newInstance);
    RunListenerImpl.get().invalidateCache();
  }
  
  public void stop() {
    super.stop();
    RunListenerImpl.get().invalidateCache();
  }
}
