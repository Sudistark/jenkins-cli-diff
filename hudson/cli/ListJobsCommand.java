package hudson.cli;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.View;
import java.util.Collection;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;

@Extension
public class ListJobsCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Name of the view", required = false)
  public String name;
  
  public String getShortDescription() { return Messages.ListJobsCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Collection<TopLevelItem> jobs;
    Jenkins h = Jenkins.get();
    if (this.name != null) {
      View view = h.getView(this.name);
      if (view != null) {
        jobs = view.getAllItems();
      } else {
        Item item = h.getItemByFullName(this.name);
        if (item instanceof ModifiableTopLevelItemGroup) {
          jobs = ((ModifiableTopLevelItemGroup)item).getItems();
        } else {
          throw new IllegalArgumentException("No view or item group with the given name '" + this.name + "' found.");
        } 
      } 
    } else {
      jobs = h.getItems();
    } 
    for (TopLevelItem item : jobs)
      this.stdout.println(item.getName()); 
    return 0;
  }
}
