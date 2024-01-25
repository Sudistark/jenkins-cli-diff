package hudson.util.jna;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Kernel32Utils {
  public static int waitForExitProcess(Pointer hProcess) throws InterruptedException {
    int v;
    do {
      if (Thread.interrupted())
        throw new InterruptedException(); 
      Kernel32.INSTANCE.WaitForSingleObject(hProcess, 1000);
      IntByReference exitCode = new IntByReference();
      exitCode.setValue(-1);
      Kernel32.INSTANCE.GetExitCodeProcess(hProcess, exitCode);
      v = exitCode.getValue();
    } while (v == 259);
    return v;
  }
  
  @Deprecated
  public static int getWin32FileAttributes(File file) throws IOException {
    String path, canonicalPath = file.getCanonicalPath();
    if (canonicalPath.length() < 260) {
      path = canonicalPath;
    } else if (canonicalPath.startsWith("\\\\")) {
      path = "\\\\?\\UNC\\" + canonicalPath.substring(2);
    } else {
      path = "\\\\?\\" + canonicalPath;
    } 
    return Kernel32.INSTANCE.GetFileAttributesW(new WString(path));
  }
  
  @Deprecated
  public static void createSymbolicLink(File symlink, String target, boolean dirLink) throws IOException {
    if (!Kernel32.INSTANCE.CreateSymbolicLinkW(new WString(symlink
          .getPath()), new WString(target), 
        dirLink ? 1 : 0))
      throw new WinIOException("Failed to create a symlink " + symlink + " to " + target); 
  }
  
  @Deprecated
  public static boolean isJunctionOrSymlink(File file) throws IOException { return Util.isSymlink(file); }
  
  public static File getTempDir() {
    buf = new Memory(1024L);
    if (Kernel32.INSTANCE.GetTempPathW(512, buf) != 0)
      return new File(buf.getWideString(0L)); 
    return null;
  }
  
  static Kernel32 load() {
    try {
      return (Kernel32)Native.load("kernel32", Kernel32.class);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to load Kernel32", e);
      return (Kernel32)InitializationErrorInvocationHandler.create(Kernel32.class, e);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(Kernel32Utils.class.getName());
}
