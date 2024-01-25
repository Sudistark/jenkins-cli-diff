package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class ViewJob<JobT extends ViewJob<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Job<JobT, RunT> {
  private static final Logger LOGGER = Logger.getLogger(ViewJob.class.getName());
  
  private long nextUpdate = 0L;
  
  private static ReloadThread reloadThread;
  
  static void interruptReloadThread() {
    if (reloadThread != null)
      reloadThread.interrupt(); 
  }
  
  @Deprecated
  protected ViewJob(Jenkins parent, String name) { super(parent, name); }
  
  protected ViewJob(ItemGroup parent, String name) { super(parent, name); }
  
  public boolean isBuildable() { return false; }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    super.onLoad(parent, name);
    this.notLoaded = true;
  }
  
  protected SortedMap<Integer, RunT> _getRuns() {
    if (this.notLoaded || this.runs == null)
      synchronized (this) {
        if (this.runs == null)
          this.runs = new RunMap(); 
        if (this.notLoaded) {
          this.notLoaded = false;
          _reload();
        } 
      }  
    if (this.nextUpdate < System.currentTimeMillis() && 
      !this.reloadingInProgress) {
      Set<ViewJob> reloadQueue;
      this.reloadingInProgress = true;
      synchronized (ViewJob.class) {
        if (reloadThread == null) {
          reloadThread = new ReloadThread();
          reloadThread.start();
        } 
        reloadQueue = reloadThread.reloadQueue;
      } 
      synchronized (reloadQueue) {
        reloadQueue.add(this);
        reloadQueue.notify();
      } 
    } 
    return this.runs;
  }
  
  public void removeRun(RunT run) {
    if (this.runs != null && !this.runs.remove(run))
      LOGGER.log(Level.WARNING, "{0} did not contain {1} to begin with", new Object[] { this, run }); 
  }
  
  private void _reload() {
    try {
      reload();
    } finally {
      this.reloadingInProgress = false;
      this.nextUpdate = reloadPeriodically ? (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1L)) : Float.MAX_VALUE;
    } 
  }
  
  protected abstract void reload();
  
  protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    super.submit(req, rsp);
    this.nextUpdate = 0L;
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean reloadPeriodically = SystemProperties.getBoolean(ViewJob.class.getName() + ".reloadPeriodically");
}
