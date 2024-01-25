package hudson.util.jna;

import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {
  public static final Kernel32 INSTANCE = Kernel32Utils.load();
  
  public static final int MOVEFILE_COPY_ALLOWED = 2;
  
  public static final int MOVEFILE_CREATE_HARDLINK = 16;
  
  public static final int MOVEFILE_DELAY_UNTIL_REBOOT = 4;
  
  public static final int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 32;
  
  public static final int MOVEFILE_REPLACE_EXISTING = 1;
  
  public static final int MOVEFILE_WRITE_THROUGH = 8;
  
  public static final int FILE_ATTRIBUTE_REPARSE_POINT = 1024;
  
  public static final int SYMBOLIC_LINK_FLAG_DIRECTORY = 1;
  
  public static final int STILL_ACTIVE = 259;
  
  boolean MoveFileExA(String paramString1, String paramString2, int paramInt);
  
  int WaitForSingleObject(Pointer paramPointer, int paramInt);
  
  int GetFileAttributesW(WString paramWString);
  
  boolean GetExitCodeProcess(Pointer paramPointer, IntByReference paramIntByReference);
  
  boolean CreateSymbolicLinkW(WString paramWString1, WString paramWString2, int paramInt);
  
  int GetTempPathW(int paramInt, Pointer paramPointer);
}
