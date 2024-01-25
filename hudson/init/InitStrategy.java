package hudson.init;

import hudson.PluginManager;
import hudson.util.DirScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.jvnet.hudson.reactor.Task;

public class InitStrategy {
  public List<File> listPluginArchives(PluginManager pm) throws IOException {
    List<File> r = new ArrayList<File>();
    getBundledPluginsFromProperty(r);
    listPluginFiles(pm, ".jpl", r);
    listPluginFiles(pm, ".hpl", r);
    listPluginFiles(pm, ".jpi", r);
    listPluginFiles(pm, ".hpi", r);
    return r;
  }
  
  private void listPluginFiles(PluginManager pm, String extension, Collection<File> all) throws IOException {
    File[] files = pm.rootDir.listFiles(new FilterByExtension(new String[] { extension }));
    if (files == null)
      throw new IOException("Jenkins is unable to create " + pm.rootDir + "\nPerhaps its security privilege is insufficient"); 
    List<File> pluginFiles = new ArrayList<File>();
    pluginFiles.addAll(List.of(files));
    pluginFiles.sort(Comparator.comparing(File::getName));
    all.addAll(pluginFiles);
  }
  
  protected void getBundledPluginsFromProperty(List<File> r) {
    String hplProperty = SystemProperties.getString("hudson.bundled.plugins");
    if (hplProperty != null) {
      List<File> pluginFiles = new ArrayList<File>();
      for (String hplLocation : hplProperty.split(",")) {
        File hpl = new File(hplLocation.trim());
        if (hpl.exists()) {
          pluginFiles.add(hpl);
        } else if (hpl.getName().contains("*")) {
          try {
            (new DirScanner.Glob(hpl.getName(), null)).scan(hpl.getParentFile(), new Object(this, pluginFiles));
          } catch (IOException x) {
            LOGGER.log(Level.WARNING, "could not expand " + hplLocation, x);
          } 
        } else {
          LOGGER.warning("bundled plugin " + hplLocation + " does not exist");
        } 
      } 
      pluginFiles.sort(Comparator.comparing(File::getName));
      r.addAll(pluginFiles);
    } 
  }
  
  public boolean skipInitTask(Task task) { return false; }
  
  public static InitStrategy get(ClassLoader cl) throws IOException {
    Iterator<InitStrategy> it = ServiceLoader.load(InitStrategy.class, cl).iterator();
    if (!it.hasNext())
      return new InitStrategy(); 
    InitStrategy s = (InitStrategy)it.next();
    LOGGER.log(Level.FINE, "Using {0} as InitStrategy", s);
    return s;
  }
  
  private static final Logger LOGGER = Logger.getLogger(InitStrategy.class.getName());
}
