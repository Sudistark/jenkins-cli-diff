package hudson.util.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;

public interface GNUCLibrary extends Library {
  public static final int F_GETFD = 1;
  
  public static final int F_SETFD = 2;
  
  public static final int FD_CLOEXEC = 1;
  
  public static final GNUCLibrary LIBC = (GNUCLibrary)Native.load("c", GNUCLibrary.class);
  
  int fork();
  
  int kill(int paramInt1, int paramInt2);
  
  int setsid();
  
  int umask(int paramInt);
  
  int getpid();
  
  int geteuid();
  
  int getegid();
  
  int getppid();
  
  int chdir(String paramString);
  
  int getdtablesize();
  
  int execv(String paramString, StringArray paramStringArray);
  
  int execvp(String paramString, StringArray paramStringArray);
  
  int setenv(String paramString1, String paramString2, int paramInt);
  
  int unsetenv(String paramString);
  
  void perror(String paramString);
  
  String strerror(int paramInt);
  
  int fcntl(int paramInt1, int paramInt2);
  
  int fcntl(int paramInt1, int paramInt2, int paramInt3);
  
  int chown(String paramString, int paramInt1, int paramInt2);
  
  int chmod(String paramString, int paramInt);
  
  int open(String paramString, int paramInt);
  
  int dup(int paramInt);
  
  int dup2(int paramInt1, int paramInt2);
  
  long pread(int paramInt, Memory paramMemory, NativeLong paramNativeLong1, NativeLong paramNativeLong2) throws LastErrorException;
  
  int close(int paramInt);
  
  int rename(String paramString1, String paramString2);
  
  @Deprecated
  int sysctlbyname(String paramString, Pointer paramPointer1, IntByReference paramIntByReference1, Pointer paramPointer2, IntByReference paramIntByReference2);
  
  int sysctlbyname(String paramString, Pointer paramPointer1, NativeLongByReference paramNativeLongByReference, Pointer paramPointer2, NativeLong paramNativeLong);
  
  @Deprecated
  int sysctl(int[] paramArrayOfInt, int paramInt, Pointer paramPointer1, IntByReference paramIntByReference1, Pointer paramPointer2, IntByReference paramIntByReference2);
  
  int sysctl(int[] paramArrayOfInt, int paramInt, Pointer paramPointer1, NativeLongByReference paramNativeLongByReference, Pointer paramPointer2, NativeLong paramNativeLong);
  
  @Deprecated
  int sysctlnametomib(String paramString, Pointer paramPointer, IntByReference paramIntByReference);
  
  int sysctlnametomib(String paramString, Pointer paramPointer, NativeLongByReference paramNativeLongByReference);
  
  int symlink(String paramString1, String paramString2);
  
  int readlink(String paramString, Memory paramMemory, NativeLong paramNativeLong);
}
