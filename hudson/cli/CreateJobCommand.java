package hudson.cli;

import hudson.Extension;
import hudson.model.Item;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;

@Extension
public class CreateJobCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Name of the job to create", required = true)
  public String name;
  
  public String getShortDescription() { return Messages.CreateJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins h = Jenkins.get();
    if (h.getItemByFullName(this.name) != null)
      throw new IllegalStateException("Job '" + this.name + "' already exists"); 
    ModifiableTopLevelItemGroup modifiableTopLevelItemGroup = h;
    int i = this.name.lastIndexOf('/');
    if (i > 0) {
      String group = this.name.substring(0, i);
      Item item = h.getItemByFullName(group);
      if (item == null)
        throw new IllegalArgumentException("Unknown ItemGroup " + group); 
      if (item instanceof ModifiableTopLevelItemGroup) {
        modifiableTopLevelItemGroup = (ModifiableTopLevelItemGroup)item;
      } else {
        throw new IllegalStateException("Can't create job from CLI in " + group);
      } 
      this.name = this.name.substring(i + 1);
    } 
    Jenkins.checkGoodName(this.name);
    modifiableTopLevelItemGroup.createProjectFromXML(this.name, this.stdin);
    return 0;
  }
}
