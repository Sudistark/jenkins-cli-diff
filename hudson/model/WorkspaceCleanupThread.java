package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"workspaceCleanup"})
public class WorkspaceCleanupThread extends AsyncPeriodicWork {
  public WorkspaceCleanupThread() { super("Workspace clean-up"); }
  
  public long getRecurrencePeriod() { return recurrencePeriodHours * 3600000L; }
  
  public static void invoke() { ((WorkspaceCleanupThread)ExtensionList.lookup(AsyncPeriodicWork.class).get(WorkspaceCleanupThread.class)).run(); }
  
  protected void execute(TaskListener listener) throws InterruptedException, IOException {
    if (disabled) {
      LOGGER.fine("Disabled. Skipping execution");
      return;
    } 
    List<Node> nodes = new ArrayList<Node>();
    Jenkins j = Jenkins.get();
    nodes.add(j);
    nodes.addAll(j.getNodes());
    for (TopLevelItem item : j.allItems(TopLevelItem.class)) {
      if (item instanceof jenkins.model.ModifiableTopLevelItemGroup)
        continue; 
      listener.getLogger().println("Checking " + item.getFullDisplayName());
      for (Node node : nodes) {
        boolean check;
        FilePath ws = node.getWorkspaceFor(item);
        if (ws == null)
          continue; 
        try {
          check = shouldBeDeleted(item, ws, node);
        } catch (IOException|InterruptedException x) {
          Functions.printStackTrace(x, listener.error("Failed to check " + node.getDisplayName()));
          continue;
        } 
        if (check) {
          listener.getLogger().println("Deleting " + ws + " on " + node.getDisplayName());
          try {
            ws.deleteSuffixesRecursive();
            ws.deleteRecursive();
          } catch (IOException|InterruptedException x) {
            Functions.printStackTrace(x, listener.error("Failed to delete " + ws + " on " + node.getDisplayName()));
          } 
        } 
      } 
    } 
  }
  
  private boolean shouldBeDeleted(@NonNull TopLevelItem item, FilePath dir, @NonNull Node n) throws IOException, InterruptedException {
    if (!dir.exists()) {
      LOGGER.log(Level.FINE, "Directory {0} does not exist", dir);
      return false;
    } 
    long now = (new Date()).getTime();
    if (dir.lastModified() + retainForDays * 86400000L > now) {
      LOGGER.log(Level.FINE, "Directory {0} is only {1} old, so not deleting", new Object[] { dir, Util.getTimeSpanString(now - dir.lastModified()) });
      return false;
    } 
    if (item instanceof AbstractProject) {
      AbstractProject<?, ?> p = (AbstractProject)item;
      Node lb = p.getLastBuiltOn();
      LOGGER.log(Level.FINER, "Directory {0} is last built on {1}", new Object[] { dir, lb });
      if (lb != null && lb.equals(n)) {
        LOGGER.log(Level.FINE, "Directory {0} is the last workspace for {1}", new Object[] { dir, p });
        return false;
      } 
      if (!p.getScm().processWorkspaceBeforeDeletion(p, dir, n)) {
        LOGGER.log(Level.FINE, "Directory deletion of {0} is vetoed by SCM", dir);
        return false;
      } 
    } 
    if (item instanceof Job) {
      Job<?, ?> j = (Job)item;
      if (j.isBuilding()) {
        LOGGER.log(Level.FINE, "Job {0} is building, so not deleting", item.getFullDisplayName());
        return false;
      } 
    } 
    LOGGER.log(Level.FINER, "Going to delete directory {0}", dir);
    return true;
  }
  
  private static final Logger LOGGER = Logger.getLogger(WorkspaceCleanupThread.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean disabled = SystemProperties.getBoolean(WorkspaceCleanupThread.class.getName() + ".disabled");
  
  public static final int recurrencePeriodHours = SystemProperties.getInteger(WorkspaceCleanupThread.class.getName() + ".recurrencePeriodHours", Integer.valueOf(24)).intValue();
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static int retainForDays = SystemProperties.getInteger(WorkspaceCleanupThread.class.getName() + ".retainForDays", Integer.valueOf(30)).intValue();
}
