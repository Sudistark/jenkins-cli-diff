package hudson.os;

import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;

public abstract class SU {
  public static VirtualChannel start(TaskListener listener, String rootUsername, String rootPassword) throws IOException, InterruptedException {
    if (File.pathSeparatorChar == ';')
      return newLocalChannel(); 
    String os = Util.fixNull(System.getProperty("os.name"));
    if (os.equals("Linux"))
      return (new Object(listener, rootPassword))




















        
        .start(listener, rootPassword); 
    if (os.equals("SunOS"))
      return (new Object(listener, rootUsername, rootPassword))













        
        .start(listener, (rootUsername == null) ? null : rootPassword); 
    return newLocalChannel();
  }
  
  private static LocalChannel newLocalChannel() { return FilePath.localChannel; }
  
  public static <V, T extends Throwable> V execute(TaskListener listener, String rootUsername, String rootPassword, Callable<V, T> closure) throws T, IOException, InterruptedException {
    ch = start(listener, rootUsername, rootPassword);
    try {
      object = ch.call(closure);
      return (V)object;
    } finally {
      ch.close();
      ch.join(3000L);
    } 
  }
}
