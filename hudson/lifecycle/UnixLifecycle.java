package hudson.lifecycle;

import com.sun.jna.Native;
import com.sun.jna.StringArray;
import hudson.Platform;
import hudson.util.jna.GNUCLibrary;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.JavaVMArguments;

public class UnixLifecycle extends Lifecycle {
  private List<String> args;
  
  private Throwable failedToObtainArgs;
  
  public UnixLifecycle() throws IOException {
    try {
      this.args = JavaVMArguments.current();
    } catch (UnsupportedOperationException|LinkageError e) {
      this.failedToObtainArgs = e;
    } 
  }
  
  public void restart() throws IOException {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    try {
      if (jenkins != null)
        jenkins.cleanUp(); 
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to clean up. Restart will continue.", e);
    } 
    int sz = GNUCLibrary.LIBC.getdtablesize();
    for (int i = 3; i < sz; i++) {
      int flags = GNUCLibrary.LIBC.fcntl(i, 1);
      if (flags >= 0)
        GNUCLibrary.LIBC.fcntl(i, 2, flags | true); 
    } 
    String exe = (String)this.args.get(0);
    GNUCLibrary.LIBC.execvp(exe, new StringArray((String[])this.args.toArray(new String[0])));
    throw new IOException("Failed to exec '" + exe + "' " + GNUCLibrary.LIBC.strerror(Native.getLastError()));
  }
  
  public void verifyRestartable() throws IOException {
    if (Platform.isDarwin() && !Platform.isSnowLeopardOrLater())
      throw new RestartNotSupportedException("Restart is not supported on Mac OS X"); 
    if (this.args == null)
      throw new RestartNotSupportedException("Failed to obtain the command line arguments of the process", this.failedToObtainArgs); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(UnixLifecycle.class.getName());
}
