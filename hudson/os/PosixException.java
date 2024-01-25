package hudson.os;

@Deprecated
public class PosixException extends RuntimeException {
  public PosixException(String message) { super(message); }
  
  public PosixException(String message, Throwable cause) { super(message, cause); }
}
