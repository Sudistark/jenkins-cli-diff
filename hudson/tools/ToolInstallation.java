package hudson.tools;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.Saveable;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.NodeSpecific;
import hudson.util.DescribableList;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.Timer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public abstract class ToolInstallation extends AbstractDescribableImpl<ToolInstallation> implements Serializable, ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(ToolInstallation.class.getName());
  
  private final String name;
  
  private String home;
  
  private DescribableList<ToolProperty<?>, ToolPropertyDescriptor> properties;
  
  @Deprecated
  protected ToolInstallation(String name, String home) {
    this.properties = new DescribableList(Saveable.NOOP);
    this.name = name;
    this.home = home;
  }
  
  protected ToolInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
    this.properties = new DescribableList(Saveable.NOOP);
    this.name = name;
    this.home = home;
    if (properties != null)
      try {
        this.properties.replaceBy(properties);
        for (ToolProperty<?> p : properties)
          _setTool(p, this); 
      } catch (IOException e) {
        throw new AssertionError(e);
      }  
  }
  
  private <T extends ToolInstallation> void _setTool(ToolProperty<T> prop, ToolInstallation t) { prop.setTool((ToolInstallation)prop.type().cast(t)); }
  
  public String getName() { return this.name; }
  
  @CheckForNull
  public String getHome() { return this.home; }
  
  public void buildEnvVars(EnvVars env) {}
  
  public DescribableList<ToolProperty<?>, ToolPropertyDescriptor> getProperties() {
    if (this.properties == null)
      this.properties = new DescribableList(Saveable.NOOP); 
    return this.properties;
  }
  
  public ToolInstallation translate(@NonNull Node node, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
    ToolInstallation t = this;
    if (t instanceof NodeSpecific) {
      NodeSpecific n = (NodeSpecific)t;
      t = (ToolInstallation)n.forNode(node, listener);
    } 
    if (t instanceof EnvironmentSpecific) {
      EnvironmentSpecific e = (EnvironmentSpecific)t;
      t = (ToolInstallation)e.forEnvironment(envs);
    } 
    return t;
  }
  
  public ToolInstallation translate(AbstractBuild<?, ?> buildInProgress, TaskListener listener) throws IOException, InterruptedException {
    assert buildInProgress.isBuilding();
    return translate(buildInProgress.getBuiltOn(), buildInProgress.getEnvironment(listener), listener);
  }
  
  protected String translateFor(Node node, TaskListener log) throws IOException, InterruptedException { return ToolLocationNodeProperty.getToolHome(node, this, log); }
  
  @SuppressFBWarnings(value = {"IS2_INCONSISTENT_SYNC"}, justification = "nothing should be competing with XStream during deserialization")
  protected Object readResolve() {
    if (this.properties != null)
      for (ToolProperty<?> p : this.properties)
        _setTool(p, this);  
    return this;
  }
  
  protected Object writeReplace() {
    if (Channel.current() == null)
      return this; 
    LOGGER.log(Level.WARNING, "Serialization of " + getClass().getSimpleName() + " extends ToolInstallation over Remoting is deprecated", new Throwable());
    String xml1 = (String)Timer.get().submit(() -> Jenkins.XSTREAM2.toXML(this)).get();
    Document dom = (new SAXReader()).read(new StringReader(xml1));
    Element properties = dom.getRootElement().element("properties");
    if (properties != null)
      dom.getRootElement().remove(properties); 
    String xml2 = dom.asXML();
    return (ToolInstallation)Timer.get().submit(() -> Jenkins.XSTREAM2.fromXML(xml2)).get();
  }
  
  public String toString() {
    return getClass().getSimpleName() + "[" + getClass().getSimpleName() + "]";
  }
  
  public static DescriptorExtensionList<ToolInstallation, ToolDescriptor<?>> all() { return Jenkins.get().getDescriptorList(ToolInstallation.class); }
}
