package jenkins.util.groovy;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

public class GroovyHookScript {
  private static final String ROOT_PATH = SystemProperties.getString(GroovyHookScript.class.getName() + ".ROOT_PATH");
  
  private final String hook;
  
  private final Binding bindings;
  
  private final ServletContext servletContext;
  
  private final File rootDir;
  
  private final ClassLoader loader;
  
  @Deprecated
  public GroovyHookScript(String hook) { this(hook, Jenkins.get()); }
  
  private GroovyHookScript(String hook, Jenkins j) { this(hook, j.servletContext, j.getRootDir(), (j.getPluginManager()).uberClassLoader); }
  
  public GroovyHookScript(String hook, @NonNull ServletContext servletContext, @NonNull File jenkinsHome, @NonNull ClassLoader loader) {
    this.bindings = new Binding();
    this.hook = hook;
    this.servletContext = servletContext;
    this.rootDir = (ROOT_PATH != null) ? new File(ROOT_PATH) : jenkinsHome;
    this.loader = loader;
  }
  
  public GroovyHookScript bind(String name, Object o) {
    this.bindings.setProperty(name, o);
    return this;
  }
  
  public Binding getBindings() { return this.bindings; }
  
  public void run() {
    String hookGroovy = this.hook + ".groovy";
    String hookGroovyD = this.hook + ".groovy.d";
    try {
      URL bundled = this.servletContext.getResource("/WEB-INF/" + hookGroovy);
      execute(bundled);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to execute /WEB-INF/" + hookGroovy, e);
    } 
    Set<String> resources = this.servletContext.getResourcePaths("/WEB-INF/" + hookGroovyD + "/");
    if (resources != null)
      for (String res : new TreeSet(resources)) {
        try {
          URL bundled = this.servletContext.getResource(res);
          execute(bundled);
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Failed to execute " + res, e);
        } 
      }  
    File script = new File(this.rootDir, hookGroovy);
    execute(script);
    File scriptD = new File(this.rootDir, hookGroovyD);
    if (scriptD.isDirectory()) {
      File[] scripts = scriptD.listFiles(f -> f.getName().endsWith(".groovy"));
      if (scripts != null) {
        Arrays.sort(scripts);
        for (File f : scripts)
          execute(f); 
      } 
    } 
  }
  
  protected void execute(URL bundled) throws IOException {
    if (bundled != null) {
      LOGGER.info("Executing bundled script: " + bundled);
      execute(new GroovyCodeSource(bundled));
    } 
  }
  
  protected void execute(File f) {
    if (f.exists()) {
      LOGGER.info("Executing " + f);
      try {
        execute(new GroovyCodeSource(f));
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to execute " + f, e);
      } 
    } 
  }
  
  @SuppressFBWarnings(value = {"GROOVY_SHELL"}, justification = "Groovy hook scripts are a feature, not a bug")
  protected void execute(GroovyCodeSource s) {
    try {
      createShell().evaluate(s);
    } catch (RuntimeException x) {
      LOGGER.log(Level.WARNING, "Failed to run script " + s.getName(), x);
    } 
  }
  
  protected GroovyShell createShell() { return new GroovyShell(this.loader, this.bindings); }
  
  private static final Logger LOGGER = Logger.getLogger(GroovyHookScript.class.getName());
}
