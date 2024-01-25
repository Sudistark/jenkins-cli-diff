package hudson.tools;

import hudson.FilePath;
import hudson.Util;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;
import java.net.URL;
import org.kohsuke.stapler.DataBoundConstructor;

public class ZipExtractionInstaller extends ToolInstaller {
  private final String url;
  
  private final String subdir;
  
  @DataBoundConstructor
  public ZipExtractionInstaller(String label, String url, String subdir) {
    super(label);
    this.url = url;
    this.subdir = Util.fixEmptyAndTrim(subdir);
  }
  
  public String getUrl() { return this.url; }
  
  public String getSubdir() { return this.subdir; }
  
  public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
    FilePath dir = preferredLocation(tool, node);
    if (dir.installIfNecessaryFrom(new URL(this.url), log, "Unpacking " + this.url + " to " + dir + " on " + node.getDisplayName()))
      dir.act(new ChmodRecAPlusX()); 
    if (this.subdir == null)
      return dir; 
    return dir.child(this.subdir);
  }
}
