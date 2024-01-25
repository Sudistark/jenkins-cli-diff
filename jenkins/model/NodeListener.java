package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Node;
import java.util.List;
import java.util.logging.Logger;
import jenkins.util.Listeners;

public abstract class NodeListener implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(NodeListener.class.getName());
  
  protected void onCreated(@NonNull Node node) {}
  
  protected void onUpdated(@NonNull Node oldOne, @NonNull Node newOne) {}
  
  protected void onDeleted(@NonNull Node node) {}
  
  public static void fireOnCreated(@NonNull Node node) { Listeners.notify(NodeListener.class, false, l -> l.onCreated(node)); }
  
  public static void fireOnUpdated(@NonNull Node oldOne, @NonNull Node newOne) { Listeners.notify(NodeListener.class, false, l -> l.onUpdated(oldOne, newOne)); }
  
  public static void fireOnDeleted(@NonNull Node node) { Listeners.notify(NodeListener.class, false, l -> l.onDeleted(node)); }
  
  @NonNull
  public static List<NodeListener> all() { return ExtensionList.lookup(NodeListener.class); }
}
