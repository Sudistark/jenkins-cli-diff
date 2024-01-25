package hudson.tools;

import hudson.DescriptorExtensionList;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Descriptor;
import hudson.model.labels.LabelExpression;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;

public abstract class ToolInstallerDescriptor<T extends ToolInstaller> extends Descriptor<ToolInstaller> {
  public boolean isApplicable(Class<? extends ToolInstallation> toolType) { return true; }
  
  public static DescriptorExtensionList<ToolInstaller, ToolInstallerDescriptor<?>> all() { return Jenkins.get().getDescriptorList(ToolInstaller.class); }
  
  public static List<ToolInstallerDescriptor<?>> for_(Class<? extends ToolInstallation> type) {
    List<ToolInstallerDescriptor<?>> r = new ArrayList<ToolInstallerDescriptor<?>>();
    for (ToolInstallerDescriptor<?> d : all()) {
      if (d.isApplicable(type))
        r.add(d); 
    } 
    return r;
  }
  
  public AutoCompletionCandidates doAutoCompleteLabel(@QueryParameter String value) { return LabelExpression.autoComplete(value); }
  
  public FormValidation doCheckLabel(@QueryParameter String value) { return LabelExpression.validate(value); }
}
