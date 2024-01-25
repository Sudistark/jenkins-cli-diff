package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Item;
import java.util.HashSet;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class DeleteJobCommand extends CLICommand {
  @Argument(usage = "Name of the job(s) to delete", required = true, multiValued = true)
  private List<String> jobs;
  
  public String getShortDescription() { return Messages.DeleteJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    boolean errorOccurred = false;
    Jenkins jenkins = Jenkins.get();
    HashSet<String> hs = new HashSet<String>(this.jobs);
    for (String job_s : hs) {
      try {
        AbstractItem job = (AbstractItem)jenkins.getItemByFullName(job_s);
        if (job == null)
          throw new IllegalArgumentException("No such job '" + job_s + "'"); 
        job.checkPermission(Item.DELETE);
        job.delete();
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
