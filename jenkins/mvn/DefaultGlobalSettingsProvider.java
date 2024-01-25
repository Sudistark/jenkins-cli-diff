package jenkins.mvn;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

public class DefaultGlobalSettingsProvider extends GlobalSettingsProvider {
  public FilePath supplySettings(AbstractBuild<?, ?> project, TaskListener listener) { return null; }
}
