package jenkins;

import hudson.PluginWrapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MissingDependencyException extends IOException {
  private String pluginShortName;
  
  private List<PluginWrapper.Dependency> missingDependencies;
  
  public MissingDependencyException(String pluginShortName, List<PluginWrapper.Dependency> missingDependencies) {
    super("One or more dependencies could not be resolved for " + pluginShortName + " : " + (String)missingDependencies
        .stream().map(Object::toString).collect(Collectors.joining(", ")));
    this.pluginShortName = pluginShortName;
    this.missingDependencies = missingDependencies;
  }
  
  public List<PluginWrapper.Dependency> getMissingDependencies() { return this.missingDependencies; }
  
  public String getPluginShortName() { return this.pluginShortName; }
}
