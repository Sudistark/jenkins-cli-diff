package jenkins.mvn;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

public class DefaultSettingsProvider extends SettingsProvider {
  public FilePath supplySettings(AbstractBuild<?, ?> project, TaskListener listener) { return null; }
}
