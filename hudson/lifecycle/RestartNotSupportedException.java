package hudson.lifecycle;

public class RestartNotSupportedException extends Exception {
  public RestartNotSupportedException(String reason) { super(reason); }
  
  public RestartNotSupportedException(String reason, Throwable cause) { super(reason, cause); }
}
