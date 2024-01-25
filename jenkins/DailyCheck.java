package jenkins;

import hudson.Extension;
import hudson.Main;
import hudson.model.AsyncPeriodicWork;
import hudson.model.DownloadService;
import hudson.model.TaskListener;
import hudson.model.UpdateSite;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Symbol({"updateCenterCheck"})
public final class DailyCheck extends AsyncPeriodicWork {
  private static final Logger LOGGER = Logger.getLogger(DailyCheck.class.getName());
  
  public DailyCheck() { super("Download metadata"); }
  
  public long getRecurrencePeriod() { return 86400000L; }
  
  public long getInitialDelay() { return Main.isUnitTest ? 86400000L : 0L; }
  
  protected void execute(TaskListener listener) throws IOException, InterruptedException {
    boolean due = false;
    for (UpdateSite site : Jenkins.get().getUpdateCenter().getSites()) {
      if (site.isDue()) {
        due = true;
        break;
      } 
    } 
    if (!due) {
      long now = System.currentTimeMillis();
      for (DownloadService.Downloadable d : DownloadService.Downloadable.all()) {
        if (d.getDue() <= now)
          try {
            d.updateNow();
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("Unable to update downloadable [%s]", new Object[] { d.getId() }), e);
          }  
      } 
      return;
    } 
    HttpResponse rsp = Jenkins.get().getPluginManager().doCheckUpdatesServer();
    if (rsp instanceof FormValidation)
      listener.error(((FormValidation)rsp).renderHtml()); 
  }
}
