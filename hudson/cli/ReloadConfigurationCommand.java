package hudson.cli;

import hudson.Extension;
import hudson.util.JenkinsReloadFailed;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.WebApp;

@Extension
public class ReloadConfigurationCommand extends CLICommand {
  public String getShortDescription() { return Messages.ReloadConfigurationCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins j = Jenkins.get();
    j.doReload();
    Object app;
    while (app = WebApp.get(j.servletContext).getApp() instanceof hudson.util.HudsonIsLoading)
      Thread.sleep(100L); 
    if (app instanceof Jenkins)
      return 0; 
    if (app instanceof JenkinsReloadFailed) {
      Throwable t = ((JenkinsReloadFailed)app).cause;
      if (t instanceof Exception)
        throw (Exception)t; 
      throw new RuntimeException(t);
    } 
    this.stderr.println("Unexpected status " + app);
    return 1;
  }
}
