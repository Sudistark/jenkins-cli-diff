package jenkins.slaves.restarter;

import com.sun.jna.Native;
import com.sun.jna.StringArray;
import hudson.Extension;
import hudson.util.jna.GNUCLibrary;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.JavaVMArguments;

@Extension
public class UnixSlaveRestarter extends SlaveRestarter {
  private List<String> args;
  
  public boolean canWork() {
    try {
      if (File.pathSeparatorChar != ':')
        return false; 
      this.args = JavaVMArguments.current();
      GNUCLibrary.LIBC.getdtablesize();
      int v = GNUCLibrary.LIBC.fcntl(99999, 1);
      GNUCLibrary.LIBC.fcntl(99999, 2, v);
      getCurrentExecutable();
      GNUCLibrary.LIBC.execv("positively/no/such/executable", new StringArray(new String[] { "a", "b", "c" }));
      return true;
    } catch (UnsupportedOperationException|LinkageError e) {
      LOGGER.log(Level.FINE, "" + getClass() + " unsuitable", e);
      return false;
    } 
  }
  
  public void restart() {
    int sz = GNUCLibrary.LIBC.getdtablesize();
    for (int i = 3; i < sz; i++) {
      int flags = GNUCLibrary.LIBC.fcntl(i, 1);
      if (flags >= 0)
        GNUCLibrary.LIBC.fcntl(i, 2, flags | true); 
    } 
    String exe = getCurrentExecutable();
    GNUCLibrary.LIBC.execv(exe, new StringArray((String[])this.args.toArray(new String[0])));
    throw new IOException("Failed to exec '" + exe + "' " + GNUCLibrary.LIBC.strerror(Native.getLastError()));
  }
  
  private static String getCurrentExecutable() {
    info = ProcessHandle.current().info();
    if (info.command().isPresent())
      return (String)info.command().get(); 
    long pid = ProcessHandle.current().pid();
    String name = "/proc/" + pid + "/exe";
    try {
      Path exe = Paths.get(name, new String[0]);
      if (Files.exists(exe, new java.nio.file.LinkOption[0])) {
        if (Files.isSymbolicLink(exe))
          return Files.readSymbolicLink(exe).toString(); 
        return exe.toString();
      } 
    } catch (IOException|java.nio.file.InvalidPathException|UnsupportedOperationException e) {
      LOGGER.log(Level.FINE, "Failed to resolve " + name, e);
    } 
    return Paths.get(System.getProperty("java.home"), new String[0]).resolve("bin").resolve("java").toString();
  }
  
  private static final Logger LOGGER = Logger.getLogger(UnixSlaveRestarter.class.getName());
  
  private static final long serialVersionUID = 1L;
}
