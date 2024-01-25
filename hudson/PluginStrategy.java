package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Hudson;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface PluginStrategy extends ExtensionPoint {
  PluginWrapper createPluginWrapper(File paramFile) throws IOException;
  
  @NonNull
  String getShortName(File paramFile) throws IOException;
  
  void load(PluginWrapper paramPluginWrapper) throws IOException;
  
  void initializeComponents(PluginWrapper paramPluginWrapper) throws IOException;
  
  <T> List<ExtensionComponent<T>> findComponents(Class<T> paramClass, Hudson paramHudson);
  
  default void updateDependency(PluginWrapper depender, PluginWrapper dependee) { Logger.getLogger(PluginStrategy.class.getName()).log(Level.WARNING, "{0} does not yet implement updateDependency", getClass()); }
}
