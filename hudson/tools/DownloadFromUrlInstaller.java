package hudson.tools;

import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public abstract class DownloadFromUrlInstaller extends ToolInstaller {
  public final String id;
  
  protected DownloadFromUrlInstaller(String id) {
    super(null);
    this.id = id;
  }
  
  protected boolean isUpToDate(FilePath expectedLocation, Installable i) throws IOException, InterruptedException {
    FilePath marker = expectedLocation.child(".installedFrom");
    return (marker.exists() && marker.readToString().equals(i.url));
  }
  
  public Installable getInstallable() throws IOException {
    for (Installable i : ((DescriptorImpl)getDescriptor()).getInstallables()) {
      if (this.id.equals(i.id))
        return i; 
    } 
    return null;
  }
  
  public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
    FilePath expected = preferredLocation(tool, node);
    Installable inst = getInstallable();
    if (inst == null) {
      log.getLogger().println("Invalid tool ID " + this.id);
      return expected;
    } 
    if (inst instanceof NodeSpecific)
      inst = (Installable)((NodeSpecific)inst).forNode(node, log); 
    if (isUpToDate(expected, inst))
      return expected; 
    if (expected.installIfNecessaryFrom(new URL(inst.url), log, "Unpacking " + inst.url + " to " + expected + " on " + node.getDisplayName())) {
      expected.child(".timestamp").delete();
      FilePath base = findPullUpDirectory(expected);
      if (base != null && base != expected)
        base.moveAllChildrenTo(expected); 
      expected.child(".installedFrom").write(inst.url, "UTF-8");
      expected.act(new ZipExtractionInstaller.ChmodRecAPlusX());
    } 
    return expected;
  }
  
  protected FilePath findPullUpDirectory(FilePath root) throws IOException, InterruptedException {
    List<FilePath> children = root.list();
    if (children.size() != 1)
      return null; 
    if (((FilePath)children.get(0)).isDirectory())
      return (FilePath)children.get(0); 
    return null;
  }
}
