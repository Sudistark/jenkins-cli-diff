package hudson.tools;

import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

public class ToolLocationNodeProperty extends NodeProperty<Node> {
  private final List<ToolLocation> locations;
  
  @DataBoundConstructor
  public ToolLocationNodeProperty(List<ToolLocation> locations) {
    if (locations == null)
      locations = new ArrayList<ToolLocation>(); 
    this.locations = locations;
  }
  
  public ToolLocationNodeProperty(ToolLocation... locations) { this(Arrays.asList(locations)); }
  
  public List<ToolLocation> getLocations() { return Collections.unmodifiableList(this.locations); }
  
  public String getHome(ToolInstallation installation) {
    for (ToolLocation location : this.locations) {
      if (location.getName().equals(installation.getName()) && location.getType() == installation.getDescriptor())
        return location.getHome(); 
    } 
    return null;
  }
  
  @Deprecated
  public static String getToolHome(Node node, ToolInstallation installation, TaskListener log) throws IOException, InterruptedException {
    String result = null;
    ToolLocationNodeProperty property = (ToolLocationNodeProperty)node.getNodeProperties().get(ToolLocationNodeProperty.class);
    if (property != null)
      result = property.getHome(installation); 
    if (result != null)
      return result; 
    for (ToolLocationTranslator t : ToolLocationTranslator.all()) {
      result = t.getToolHome(node, installation, log);
      if (result != null)
        return result; 
    } 
    return installation.getHome();
  }
}
