package hudson.tools;

import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

public class InstallSourceProperty extends ToolProperty<ToolInstallation> {
  public final DescribableList<ToolInstaller, Descriptor<ToolInstaller>> installers = new DescribableList(Saveable.NOOP);
  
  @DataBoundConstructor
  public InstallSourceProperty(List<? extends ToolInstaller> installers) throws IOException {
    if (installers != null)
      this.installers.replaceBy(installers); 
  }
  
  public void setTool(ToolInstallation t) {
    super.setTool(t);
    for (ToolInstaller installer : this.installers)
      installer.setTool(t); 
  }
  
  public Class<ToolInstallation> type() { return ToolInstallation.class; }
}
