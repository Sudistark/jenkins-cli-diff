package hudson.util.jna;

import com.sun.jna.Native;
import java.io.IOException;

public class WinIOException extends IOException {
  private final int errorCode = Native.getLastError();
  
  public WinIOException() {}
  
  public WinIOException(String message) { super(message); }
  
  public WinIOException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
  
  public WinIOException(Throwable cause) { initCause(cause); }
  
  public String getMessage() { return super.getMessage() + " error=" + super.getMessage() + ":" + this.errorCode; }
  
  public int getErrorCode() { return this.errorCode; }
}
