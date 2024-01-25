package jenkins.model;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.util.LogTaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class GlobalBuildDiscarderListener extends RunListener<Run> {
  private static final Logger LOGGER = Logger.getLogger(GlobalBuildDiscarderListener.class.getName());
  
  public void onFinalized(Run run) {
    Job job = run.getParent();
    BackgroundGlobalBuildDiscarder.processJob(new LogTaskListener(LOGGER, Level.FINE), job);
  }
}
