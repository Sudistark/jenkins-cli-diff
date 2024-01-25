package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

public class LocalPluginManager extends PluginManager {
  public LocalPluginManager(@CheckForNull ServletContext context, @NonNull File rootDir) { super(context, new File(rootDir, "plugins")); }
  
  public LocalPluginManager(@NonNull Jenkins jenkins) { this(jenkins.servletContext, jenkins.getRootDir()); }
  
  public LocalPluginManager(@NonNull File rootDir) { this(null, rootDir); }
  
  protected Collection<String> loadBundledPlugins() {
    if (SystemProperties.getString("hudson.bundled.plugins") != null)
      return Collections.emptySet(); 
    try {
      return loadPluginsFromWar("/WEB-INF/plugins");
    } finally {
      loadDetachedPlugins();
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(LocalPluginManager.class.getName());
}
