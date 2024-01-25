package hudson.scm;

import hudson.model.AbstractBuild;
import hudson.model.BuildBadgeAction;
import hudson.model.Run;
import hudson.model.TaskAction;
import hudson.security.ACL;
import hudson.security.Permission;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractScmTagAction extends TaskAction implements BuildBadgeAction, RunAction2 {
  private Run<?, ?> run;
  
  @Deprecated
  protected AbstractBuild build;
  
  protected AbstractScmTagAction(Run<?, ?> run) {
    this.run = run;
    this.build = (run instanceof AbstractBuild) ? (AbstractBuild)run : null;
  }
  
  @Deprecated
  protected AbstractScmTagAction(AbstractBuild build) { this(build); }
  
  public final String getUrlName() { return "tagBuild"; }
  
  protected Permission getPermission() { return SCM.TAG; }
  
  public Run<?, ?> getRun() { return this.run; }
  
  @Deprecated
  public AbstractBuild getBuild() { return this.build; }
  
  public String getTooltip() { return null; }
  
  public abstract boolean isTagged();
  
  protected ACL getACL() { return this.run.getACL(); }
  
  public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { req.getView(this, chooseAction()).forward(req, rsp); }
  
  protected String chooseAction() {
    if (this.workerThread != null)
      return "inProgress.jelly"; 
    return "tagForm.jelly";
  }
  
  public void onAttached(Run<?, ?> r) {}
  
  public void onLoad(Run<?, ?> r) {
    this.run = r;
    this.build = (this.run instanceof AbstractBuild) ? (AbstractBuild)this.run : null;
  }
}
