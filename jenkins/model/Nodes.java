package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.BulkChange;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Nodes implements Saveable {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  private static final boolean ENFORCE_NAME_RESTRICTIONS = SystemProperties.getBoolean(Nodes.class.getName() + ".enforceNameRestrictions", true);
  
  @NonNull
  private final Jenkins jenkins;
  
  private final ConcurrentMap<String, Node> nodes;
  
  Nodes(@NonNull Jenkins jenkins) {
    this.nodes = new ConcurrentSkipListMap();
    this.jenkins = jenkins;
  }
  
  @NonNull
  public List<Node> getNodes() { return new ArrayList(this.nodes.values()); }
  
  public void setNodes(@NonNull Collection<? extends Node> nodes) throws IOException {
    Queue.withLock(new Object(this, nodes));
    save();
  }
  
  public void addNode(@NonNull Node node) throws IOException {
    if (ENFORCE_NAME_RESTRICTIONS)
      Jenkins.checkGoodName(node.getNodeName()); 
    Node oldNode = (Node)this.nodes.get(node.getNodeName());
    if (node != oldNode) {
      AtomicReference<Node> old = new AtomicReference<Node>();
      old.set((Node)this.nodes.put(node.getNodeName(), node));
      this.jenkins.updateNewComputer(node);
      this.jenkins.trimLabels(new Node[] { node, oldNode });
      try {
        persistNode(node);
      } catch (IOException|RuntimeException e) {
        Queue.withLock(new Object(this, node, oldNode));
        throw e;
      } 
      if (old.get() != null) {
        NodeListener.fireOnUpdated((Node)old.get(), node);
      } else {
        NodeListener.fireOnCreated(node);
      } 
    } 
  }
  
  private void persistNode(@NonNull Node node) throws IOException {
    if (node instanceof hudson.slaves.EphemeralNode) {
      Util.deleteRecursive(new File(getNodesDir(), node.getNodeName()));
    } else {
      XmlFile xmlFile = new XmlFile(Jenkins.XSTREAM, new File(new File(getNodesDir(), node.getNodeName()), "config.xml"));
      xmlFile.write(node);
      SaveableListener.fireOnChange(this, xmlFile);
    } 
    this.jenkins.getQueue().scheduleMaintenance();
  }
  
  public boolean updateNode(@NonNull Node node) throws IOException {
    boolean exists;
    try {
      exists = ((Boolean)Queue.withLock(new Object(this, node))).booleanValue();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      exists = false;
    } 
    if (exists) {
      persistNode(node);
      return true;
    } 
    return false;
  }
  
  public boolean replaceNode(Node oldOne, @NonNull Node newOne) throws IOException {
    if (ENFORCE_NAME_RESTRICTIONS)
      Jenkins.checkGoodName(newOne.getNodeName()); 
    if (oldOne == this.nodes.get(oldOne.getNodeName())) {
      Queue.withLock(new Object(this, oldOne, newOne));
      updateNode(newOne);
      if (!newOne.getNodeName().equals(oldOne.getNodeName()))
        Util.deleteRecursive(new File(getNodesDir(), oldOne.getNodeName())); 
      NodeListener.fireOnUpdated(oldOne, newOne);
      return true;
    } 
    return false;
  }
  
  public void removeNode(@NonNull Node node) throws IOException {
    if (node == this.nodes.get(node.getNodeName())) {
      Queue.withLock(new Object(this, node));
      Util.deleteRecursive(new File(getNodesDir(), node.getNodeName()));
      NodeListener.fireOnDeleted(node);
    } 
  }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    File nodesDir = getNodesDir();
    Set<String> existing = new HashSet<String>();
    for (Node n : this.nodes.values()) {
      if (n instanceof hudson.slaves.EphemeralNode)
        continue; 
      existing.add(n.getNodeName());
      XmlFile xmlFile = new XmlFile(Jenkins.XSTREAM, new File(new File(nodesDir, n.getNodeName()), "config.xml"));
      xmlFile.write(n);
      SaveableListener.fireOnChange(this, xmlFile);
    } 
    for (File forDeletion : nodesDir.listFiles(pathname -> 
        (pathname.isDirectory() && !existing.contains(pathname.getName()))))
      Util.deleteRecursive(forDeletion); 
  }
  
  @CheckForNull
  public Node getNode(String name) { return (name == null) ? null : (Node)this.nodes.get(name); }
  
  public void load() throws IOException {
    File nodesDir = getNodesDir();
    File[] subdirs = nodesDir.listFiles(File::isDirectory);
    Map<String, Node> newNodes = new TreeMap<String, Node>();
    if (subdirs != null)
      for (File subdir : subdirs) {
        try {
          XmlFile xmlFile = new XmlFile(Jenkins.XSTREAM, new File(subdir, "config.xml"));
          if (xmlFile.exists()) {
            Node node = (Node)xmlFile.read();
            newNodes.put(node.getNodeName(), node);
          } 
        } catch (IOException e) {
          Logger.getLogger(Nodes.class.getName()).log(Level.WARNING, "could not load " + subdir, e);
        } 
      }  
    Queue.withLock(new Object(this, newNodes));
  }
  
  private File getNodesDir() throws IOException {
    File nodesDir = new File(this.jenkins.getRootDir(), "nodes");
    if (!nodesDir.isDirectory() && !nodesDir.mkdirs())
      throw new IOException(String.format("Could not mkdirs %s", new Object[] { nodesDir })); 
    return nodesDir;
  }
  
  public boolean isLegacy() { return !(new File(this.jenkins.getRootDir(), "nodes")).isDirectory(); }
}
