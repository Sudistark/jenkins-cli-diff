package hudson.util.jna;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface Shell32 extends StdCallLibrary {
  public static final Shell32 INSTANCE = (Shell32)Native.load("shell32", Shell32.class);
  
  boolean ShellExecuteEx(SHELLEXECUTEINFO paramSHELLEXECUTEINFO);
}
