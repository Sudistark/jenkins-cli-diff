package hudson.lifecycle;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.util.StreamTaskListener;
import hudson.util.jna.Kernel32;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;

public class WindowsServiceLifecycle extends Lifecycle {
  public WindowsServiceLifecycle() { updateJenkinsExeIfNeeded(); }
  
  private void updateJenkinsExeIfNeeded() {
    try {
      File baseDir = getBaseDir();
      URL exe = getClass().getResource("/windows-service/jenkins.exe");
      String ourCopy = Util.getDigestOf(exe.openStream());
      for (String name : new String[] { "hudson.exe", "jenkins.exe" }) {
        try {
          File currentCopy = new File(baseDir, name);
          if (currentCopy.exists()) {
            String curCopy = (new FilePath(currentCopy)).digest();
            if (!ourCopy.equals(curCopy)) {
              File stage = new File(baseDir, name + ".new");
              FileUtils.copyURLToFile(exe, stage);
              Kernel32.INSTANCE.MoveFileExA(stage.getAbsolutePath(), currentCopy.getAbsolutePath(), 5);
              LOGGER.info("Scheduled a replacement of " + name);
            } 
          } 
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Failed to replace " + name, e);
        } catch (InterruptedException interruptedException) {}
      } 
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to replace jenkins.exe", e);
    } 
  }
  
  public void rewriteHudsonWar(File by) throws IOException {
    File dest = getHudsonWar();
    if (dest == null)
      throw new IOException("jenkins.war location is not known."); 
    File bak = new File(dest.getPath() + ".bak");
    if (!by.equals(bak))
      FileUtils.copyFile(dest, bak); 
    String baseName = dest.getName();
    baseName = baseName.substring(0, baseName.indexOf('.'));
    File baseDir = getBaseDir();
    File copyFiles = new File(baseDir, baseName + ".copies");
    Writer w = Files.newBufferedWriter(Util.fileToPath(copyFiles), Charset.defaultCharset(), new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND });
    try {
      w.write(by.getAbsolutePath() + ">" + by.getAbsolutePath() + "\n");
      if (w != null)
        w.close(); 
    } catch (Throwable throwable) {
      if (w != null)
        try {
          w.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public void restart() {
    File executable;
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    try {
      if (jenkins != null)
        jenkins.cleanUp(); 
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to clean up. Restart will continue.", e);
    } 
    File me = getHudsonWar();
    File home = me.getParentFile();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamTaskListener task = new StreamTaskListener(baos);
    task.getLogger().println("Restarting a service");
    String exe = System.getenv("WINSW_EXECUTABLE");
    if (exe != null) {
      executable = new File(exe);
    } else {
      executable = new File(home, "hudson.exe");
    } 
    if (!executable.exists())
      executable = new File(home, "jenkins.exe"); 
    int r = (new Launcher.LocalLauncher(task)).launch().cmds(executable, new String[] { "restart!" }).stdout(task).pwd(home).join();
    if (r != 0)
      throw new IOException(baos.toString()); 
  }
  
  private static File getBaseDir() {
    String baseEnv = System.getenv("BASE");
    if (baseEnv != null) {
      baseDir = new File(baseEnv);
    } else {
      LOGGER.log(Level.WARNING, "Could not find environment variable 'BASE' for Jenkins base directory. Falling back to JENKINS_HOME");
      baseDir = Jenkins.get().getRootDir();
    } 
    return baseDir;
  }
  
  private static final Logger LOGGER = Logger.getLogger(WindowsServiceLifecycle.class.getName());
}
