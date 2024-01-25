package jenkins;

import org.jvnet.localizer.Localizable;

public class RestartRequiredException extends Exception {
  public final Localizable message;
  
  public RestartRequiredException(Localizable message) { this.message = message; }
  
  public RestartRequiredException(Localizable message, Throwable cause) {
    super(cause);
    this.message = message;
  }
}
