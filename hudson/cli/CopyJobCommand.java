package hudson.cli;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import jenkins.model.Jenkins;
import jenkins.model.ModifiableTopLevelItemGroup;
import org.kohsuke.args4j.Argument;

@Extension
public class CopyJobCommand extends CLICommand {
  @Argument(metaVar = "SRC", usage = "Name of the job to copy", required = true)
  public TopLevelItem src;
  
  @Argument(metaVar = "DST", usage = "Name of the new job to be created.", index = 1, required = true)
  public String dst;
  
  public String getShortDescription() { return Messages.CopyJobCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins jenkins = Jenkins.get();
    if (jenkins.getItemByFullName(this.dst) != null)
      throw new IllegalStateException("Job '" + this.dst + "' already exists"); 
    ModifiableTopLevelItemGroup modifiableTopLevelItemGroup = jenkins;
    int i = this.dst.lastIndexOf('/');
    if (i > 0) {
      String group = this.dst.substring(0, i);
      Item item = jenkins.getItemByFullName(group);
      if (item == null)
        throw new IllegalArgumentException("Unknown ItemGroup " + group); 
      if (item instanceof ModifiableTopLevelItemGroup) {
        modifiableTopLevelItemGroup = (ModifiableTopLevelItemGroup)item;
      } else {
        throw new IllegalStateException("Can't create job from CLI in " + group);
      } 
      this.dst = this.dst.substring(i + 1);
    } 
    modifiableTopLevelItemGroup.copy(this.src, this.dst).save();
    return 0;
  }
}
