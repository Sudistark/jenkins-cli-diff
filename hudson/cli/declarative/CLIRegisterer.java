package hudson.cli.declarative;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionComponent;
import hudson.ExtensionFinder;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.Hudson;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.ExtensionComponentSet;
import jenkins.ExtensionRefreshException;
import jenkins.model.Jenkins;
import org.jvnet.hudson.annotation_indexer.Index;
import org.jvnet.localizer.ResourceBundleHolder;

@Extension
public class CLIRegisterer extends ExtensionFinder {
  public ExtensionComponentSet refresh() throws ExtensionRefreshException { return ExtensionComponentSet.EMPTY; }
  
  public <T> Collection<ExtensionComponent<T>> find(Class<T> type, Hudson jenkins) {
    if (type == CLICommand.class)
      return discover(jenkins); 
    return Collections.emptyList();
  }
  
  private Method findResolver(Class type) throws IOException {
    List<Method> resolvers = Util.filter(Index.list(CLIResolver.class, (Jenkins.get().getPluginManager()).uberClassLoader), Method.class);
    for (; type != null; type = type.getSuperclass()) {
      for (Method m : resolvers) {
        if (m.getReturnType() == type)
          return m; 
      } 
    } 
    return null;
  }
  
  private List<ExtensionComponent<CLICommand>> discover(@NonNull Jenkins jenkins) {
    LOGGER.fine("Listing up @CLIMethod");
    List<ExtensionComponent<CLICommand>> r = new ArrayList<ExtensionComponent<CLICommand>>();
    try {
      for (Method m : Util.filter(Index.list(CLIMethod.class, (jenkins.getPluginManager()).uberClassLoader), Method.class)) {
        try {
          String name = ((CLIMethod)m.getAnnotation(CLIMethod.class)).name();
          ResourceBundleHolder res = loadMessageBundle(m);
          res.format("CLI." + name + ".shortDescription", new Object[0]);
          r.add(new ExtensionComponent(new Object(this, name, res, m, jenkins)));
        } catch (ClassNotFoundException|java.util.MissingResourceException e) {
          LOGGER.log(Level.SEVERE, "Failed to process @CLIMethod: " + m, e);
        } 
      } 
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to discover @CLIMethod", e);
    } 
    return r;
  }
  
  private ResourceBundleHolder loadMessageBundle(Method m) throws ClassNotFoundException {
    Class c = m.getDeclaringClass();
    Class<?> msg = c.getClassLoader().loadClass(c.getName().substring(0, c.getName().lastIndexOf(".")) + ".Messages");
    return ResourceBundleHolder.get(msg);
  }
  
  private static final Logger LOGGER = Logger.getLogger(CLIRegisterer.class.getName());
}
