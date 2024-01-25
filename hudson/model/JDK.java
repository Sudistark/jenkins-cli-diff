package hudson.model;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

public final class JDK extends ToolInstallation implements NodeSpecific<JDK>, EnvironmentSpecific<JDK> {
  public static final String DEFAULT_NAME = "(System)";
  
  private static final long serialVersionUID = -3318291200160313357L;
  
  @Deprecated
  private String javaHome;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isDefaultName(String name) {
    if ("(Default)".equals(name))
      return true; 
    return ("(System)".equals(name) || name == null);
  }
  
  public JDK(String name, String javaHome) { super(name, javaHome, Collections.emptyList()); }
  
  @DataBoundConstructor
  public JDK(String name, String home, List<? extends ToolProperty<?>> properties) { super(name, home, properties); }
  
  @Deprecated
  public String getJavaHome() { return getHome(); }
  
  public File getBinDir() { return new File(getHome(), "bin"); }
  
  private File getExecutable() {
    String execName = (File.separatorChar == '\\') ? "java.exe" : "java";
    return new File(getHome(), "bin/" + execName);
  }
  
  public boolean getExists() { return getExecutable().exists(); }
  
  @Deprecated
  public void buildEnvVars(Map<String, String> env) {
    String home = getHome();
    if (home == null)
      return; 
    env.put("PATH+JDK", home + "/bin");
    env.put("JAVA_HOME", home);
  }
  
  public void buildEnvVars(EnvVars env) { buildEnvVars(env); }
  
  public JDK forNode(Node node, TaskListener log) throws IOException, InterruptedException { return new JDK(getName(), translateFor(node, log)); }
  
  public JDK forEnvironment(EnvVars environment) { return new JDK(getName(), environment.expand(getHome())); }
  
  public static boolean isDefaultJDKValid(Node n) {
    try {
      StreamTaskListener streamTaskListener = new StreamTaskListener(OutputStream.nullOutputStream());
      Launcher launcher = n.createLauncher(streamTaskListener);
      return (launcher.launch().cmds(new String[] { "java", "-fullversion" }).stdout(streamTaskListener).join() == 0);
    } catch (IOException|InterruptedException e) {
      return false;
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(JDK.class.getName());
}
