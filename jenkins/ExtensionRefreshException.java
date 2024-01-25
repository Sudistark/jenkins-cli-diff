package jenkins;

public class ExtensionRefreshException extends Exception {
  public ExtensionRefreshException() {}
  
  public ExtensionRefreshException(String message) { super(message); }
  
  public ExtensionRefreshException(String message, Throwable cause) { super(message, cause); }
  
  public ExtensionRefreshException(Throwable cause) { super(cause); }
}
