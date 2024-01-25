package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.PluginManager;
import hudson.model.UpdateCenter;
import hudson.model.UpdateSite;
import hudson.util.EditDistance;
import hudson.util.VersionNumber;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class InstallPluginCommand extends CLICommand {
  public String getShortDescription() { return Messages.InstallPluginCommand_ShortDescription(); }
  
  @Argument(metaVar = "SOURCE", required = true, usage = "If this is an URL, Jenkins downloads the URL and installs that as a plugin. If it is the string ‘=’, the file will be read from standard input of the command. Otherwise the name is assumed to be the short name of the plugin in the existing update center (like ‘findbugs’), and the plugin will be installed from the update center. If the short name includes a minimum version number (like ‘findbugs:1.4’), and there are multiple update centers publishing different versions, the update centers will be searched in order for the first one publishing a version that is at least the specified version.")
  public List<String> sources = new ArrayList();
  
  @Deprecated
  @Option(name = "-name", usage = "No longer used.")
  public String name;
  
  @Option(name = "-restart", usage = "Restart Jenkins upon successful installation.")
  public boolean restart;
  
  @Option(name = "-deploy", usage = "Deploy plugins right away without postponing them until the reboot.")
  public boolean dynamicLoad;
  
  protected int run() throws Exception {
    Jenkins h = Jenkins.get();
    h.checkPermission(Jenkins.ADMINISTER);
    PluginManager pm = h.getPluginManager();
    if (this.name != null)
      this.stderr.println("-name is deprecated; it is no longer necessary nor honored."); 
    for (String source : this.sources) {
      if (source.equals("=")) {
        this.stdout.println(Messages.InstallPluginCommand_InstallingPluginFromStdin());
        File f = getTmpFile();
        FileUtils.copyInputStreamToFile(this.stdin, f);
        f = moveToFinalLocation(f);
        if (this.dynamicLoad)
          pm.dynamicLoad(f); 
        continue;
      } 
      try {
        URL u = new URL(source);
        this.stdout.println(Messages.InstallPluginCommand_InstallingPluginFromUrl(u));
        File f = getTmpFile();
        FileUtils.copyURLToFile(u, f);
        f = moveToFinalLocation(f);
        if (this.dynamicLoad)
          pm.dynamicLoad(f); 
      } catch (MalformedURLException malformedURLException) {
        UpdateSite.Plugin p;
        int index = source.lastIndexOf(':');
        if (index == -1) {
          p = h.getUpdateCenter().getPlugin(source);
        } else {
          VersionNumber version = new VersionNumber(source.substring(index + 1));
          p = h.getUpdateCenter().getPlugin(source.substring(0, index), version);
          if (p == null)
            p = h.getUpdateCenter().getPlugin(source); 
        } 
        if (p != null) {
          this.stdout.println(Messages.InstallPluginCommand_InstallingFromUpdateCenter(source));
          Throwable e = ((UpdateCenter.UpdateCenterJob)p.deploy(this.dynamicLoad).get()).getError();
          if (e != null) {
            AbortException myException = new AbortException("Failed to install plugin " + source);
            myException.initCause(e);
            throw myException;
          } 
          continue;
        } 
        this.stdout.println(Messages.InstallPluginCommand_NotAValidSourceName(source));
        if (!source.contains(".") && !source.contains(":") && !source.contains("/") && !source.contains("\\"))
          if (h.getUpdateCenter().getSites().isEmpty()) {
            this.stdout.println(Messages.InstallPluginCommand_NoUpdateCenterDefined());
          } else {
            Set<String> candidates = new HashSet<String>();
            for (UpdateSite s : h.getUpdateCenter().getSites()) {
              UpdateSite.Data dt = s.getData();
              if (dt == null) {
                this.stdout.println(Messages.InstallPluginCommand_NoUpdateDataRetrieved(s.getUrl()));
                continue;
              } 
              candidates.addAll(dt.plugins.keySet());
            } 
            this.stdout.println(Messages.InstallPluginCommand_DidYouMean(source, EditDistance.findNearest(source, candidates)));
          }  
        throw new AbortException("Error occurred, see previous output.");
      } 
    } 
    if (this.restart)
      h.safeRestart(); 
    return 0;
  }
  
  private static File getTmpFile() throws Exception { return File.createTempFile("download", ".jpi.tmp", (Jenkins.get().getPluginManager()).rootDir); }
  
  private static File moveToFinalLocation(File tmpFile) throws Exception {
    String pluginName;
    JarFile jf = new JarFile(tmpFile);
    try {
      Manifest mf = jf.getManifest();
      if (mf == null)
        throw new IllegalArgumentException("JAR lacks a manifest"); 
      pluginName = mf.getMainAttributes().getValue("Short-Name");
      jf.close();
    } catch (Throwable throwable) {
      try {
        jf.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    if (pluginName == null)
      throw new IllegalArgumentException("JAR manifest lacks a Short-Name attribute and so does not look like a plugin"); 
    File target = new File((Jenkins.get().getPluginManager()).rootDir, pluginName + ".jpi");
    Files.move(tmpFile.toPath(), target.toPath(), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE });
    return target;
  }
}
