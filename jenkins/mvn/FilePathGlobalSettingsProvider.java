package jenkins.mvn;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

public class FilePathGlobalSettingsProvider extends GlobalSettingsProvider {
  private final String path;
  
  @DataBoundConstructor
  public FilePathGlobalSettingsProvider(String path) { this.path = path; }
  
  public String getPath() { return this.path; }
  
  public FilePath supplySettings(AbstractBuild<?, ?> build, TaskListener listener) {
    if (this.path == null || this.path.isEmpty())
      return null; 
    try {
      return SettingsPathHelper.getSettings(build, listener, getPath());
    } catch (Exception e) {
      throw new IllegalStateException("failed to prepare global settings.xml", e);
    } 
  }
}
