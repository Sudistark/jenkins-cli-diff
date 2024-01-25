package hudson.tools;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import jenkins.model.Jenkins;

public abstract class ToolInstaller extends Object implements Describable<ToolInstaller>, ExtensionPoint {
  private final String label;
  
  protected ToolInstallation tool;
  
  protected ToolInstaller(String label) { this.label = Util.fixEmptyAndTrim(label); }
  
  protected void setTool(ToolInstallation t) { this.tool = t; }
  
  public final String getLabel() { return this.label; }
  
  public boolean appliesTo(Node node) {
    Label l = Jenkins.get().getLabel(this.label);
    return (l == null || l.contains(node));
  }
  
  protected final FilePath preferredLocation(ToolInstallation tool, Node node) {
    if (node == null)
      throw new IllegalArgumentException("must pass non-null node"); 
    String home = Util.fixEmptyAndTrim(tool.getHome());
    if (home == null)
      home = sanitize(tool.getDescriptor().getId()) + sanitize(tool.getDescriptor().getId()) + File.separatorChar; 
    FilePath root = node.getRootPath();
    if (root == null)
      throw new IllegalArgumentException("Node " + node.getDisplayName() + " seems to be offline"); 
    return root.child("tools").child(home);
  }
  
  private String sanitize(String s) { return (s != null) ? s.replaceAll("[^A-Za-z0-9_.-]+", "_") : null; }
  
  public ToolInstallerDescriptor<?> getDescriptor() { return (ToolInstallerDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public abstract FilePath performInstallation(ToolInstallation paramToolInstallation, Node paramNode, TaskListener paramTaskListener) throws IOException, InterruptedException;
}
