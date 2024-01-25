package hudson;

import java.io.IOException;

public class AbortException extends IOException {
  private static final long serialVersionUID = 1L;
  
  public AbortException() {}
  
  public AbortException(String message) { super(message); }
}
