package hudson.util.jna;

import hudson.Util;

public class JnaException extends RuntimeException {
  private final int errorCode;
  
  public JnaException(int errorCode) {
    super("Win32 error: " + errorCode + " - " + Util.getWin32ErrorMessage(errorCode));
    this.errorCode = errorCode;
  }
  
  public int getErrorCode() { return this.errorCode; }
}
