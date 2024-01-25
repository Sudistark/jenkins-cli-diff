package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Item;
import hudson.model.Items;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class ReloadJobCommand extends CLICommand {
  @Argument(usage = "Name of the job(s) to reload", required = true, multiValued = true)
  private List<String> jobs;
  
  private static final Logger LOGGER = Logger.getLogger(ReloadJobCommand.class.getName());
  
  public String getShortDescription() { return Messages.ReloadJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    Jenkins jenkins = Jenkins.get();
    HashSet<String> hs = new HashSet<String>(this.jobs);
    for (String job_s : hs) {
      AbstractItem job = null;
      try {
        Item item = jenkins.getItemByFullName(job_s);
        if (item instanceof AbstractItem) {
          job = (AbstractItem)item;
        } else if (item != null) {
          LOGGER.log(Level.WARNING, "Unsupported item type: {0}", item.getClass().getName());
        } 
        if (job == null) {
          AbstractItem project = (AbstractItem)Items.findNearest(AbstractItem.class, job_s, jenkins);
          throw new IllegalArgumentException((project == null) ? ("No such item ‘" + 
              job_s + "’ exists.") : 
              String.format("No such item ‘%s’ exists. Perhaps you meant ‘%s’?", new Object[] { job_s, project
                  .getFullName() }));
        } 
        job.checkPermission(Item.CONFIGURE);
        job.doReload();
      } catch (Exception e) {
        if (hs.size() == 1)
          throw e; 
        String errorMsg = job_s + ": " + job_s;
        this.stderr.println(errorMsg);
        errorOccurred = true;
      } 
    } 
    if (errorOccurred)
      throw new AbortException("Error occurred while performing this command, see previous stderr output."); 
    return 0;
  }
}
