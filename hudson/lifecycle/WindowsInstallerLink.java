package hudson.lifecycle;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.ManagementLink;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import hudson.util.jna.DotNet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class WindowsInstallerLink extends ManagementLink {
  private final File hudsonWar;
  
  private WindowsInstallerLink(File jenkinsWar) { this.hudsonWar = jenkinsWar; }
  
  public String getIconFileName() { return "symbol-windows"; }
  
  public String getUrlName() { return "install"; }
  
  public String getDisplayName() { return Messages.WindowsInstallerLink_DisplayName(); }
  
  public String getDescription() { return Messages.WindowsInstallerLink_Description(); }
  
  @NonNull
  public ManagementLink.Category getCategory() { return ManagementLink.Category.CONFIGURATION; }
  
  public boolean isInstalled() { return (this.installationDir != null); }
  
  @RequirePOST
  public void doDoInstall(StaplerRequest req, StaplerResponse rsp, @QueryParameter("dir") String _dir) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (this.installationDir != null) {
      sendError("Installation is already complete", req, rsp);
      return;
    } 
    if (!DotNet.isInstalled(4, 0)) {
      sendError(".NET Framework 4.0 or later is required for this feature", req, rsp);
      return;
    } 
    File dir = (new File(_dir)).getAbsoluteFile();
    if (!dir.exists() && 
      !dir.mkdirs()) {
      sendError("Failed to create installation directory: " + dir, req, rsp);
      return;
    } 
    try {
      copy(req, rsp, dir, getClass().getResource("/windows-service/jenkins.exe"), "jenkins.exe");
      Files.deleteIfExists(Util.fileToPath(dir).resolve("jenkins.exe.config"));
      copy(req, rsp, dir, getClass().getResource("/windows-service/jenkins.xml"), "jenkins.xml");
      if (!this.hudsonWar.getCanonicalFile().equals((new File(dir, "jenkins.war")).getCanonicalFile()))
        copy(req, rsp, dir, this.hudsonWar.toURI().toURL(), "jenkins.war"); 
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamTaskListener task = new StreamTaskListener(baos);
      task.getLogger().println("Installing a service");
      int r = runElevated(new File(dir, "jenkins.exe"), "install", task, dir);
      if (r != 0) {
        sendError(baos.toString(Charset.defaultCharset()), req, rsp);
        return;
      } 
      this.installationDir = dir;
      rsp.sendRedirect(".");
    } catch (AbortException abortException) {
    
    } catch (InterruptedException e) {
      throw new ServletException(e);
    } 
  }
  
  private void copy(StaplerRequest req, StaplerResponse rsp, File dir, URL src, String name) throws ServletException, IOException {
    try {
      FileUtils.copyURLToFile(src, new File(dir, name));
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to copy " + name, e);
      sendError("Failed to copy " + name + ": " + e.getMessage(), req, rsp);
      throw new AbortException();
    } 
  }
  
  @RequirePOST
  public void doRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (this.installationDir == null) {
      rsp.sendRedirect(req.getContextPath() + "/");
      return;
    } 
    rsp.forward(this, "_restart", req);
    File oldRoot = Jenkins.get().getRootDir();
    (new Object(this, "terminator", oldRoot))
















































      
      .start();
  }
  
  protected final void sendError(Exception e, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException { sendError(e.getMessage(), req, rsp); }
  
  protected final void sendError(String message, StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
    req.setAttribute("message", message);
    req.setAttribute("pre", Boolean.valueOf(true));
    rsp.forward(Jenkins.get(), "error", req);
  }
  
  @Extension
  public static WindowsInstallerLink registerIfApplicable() {
    if (!Functions.isWindows())
      return null; 
    if (Lifecycle.get() instanceof WindowsServiceLifecycle)
      return null; 
    war = SystemProperties.getString("executable-war");
    if (war != null && (new File(war)).exists()) {
      WindowsInstallerLink link = new WindowsInstallerLink(new File(war));
      if (SystemProperties.getString(WindowsInstallerLink.class.getName() + ".prominent") != null)
        Jenkins.get().getActions().add(link); 
      return link;
    } 
    return null;
  }
  
  static int runElevated(File jenkinsExe, String command, TaskListener out, File pwd) throws IOException, InterruptedException { return (new Launcher.LocalLauncher(out)).launch().cmds(jenkinsExe, new String[] { command }).stdout(out).pwd(pwd).join(); }
  
  private static final Logger LOGGER = Logger.getLogger(WindowsInstallerLink.class.getName());
}
