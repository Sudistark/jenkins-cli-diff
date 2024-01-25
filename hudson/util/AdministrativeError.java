package hudson.util;

import hudson.model.AdministrativeMonitor;

@Deprecated
public class AdministrativeError extends AdministrativeMonitor {
  public final String message;
  
  public final String title;
  
  public final Throwable details;
  
  public AdministrativeError(String id, String title, String message, Throwable details) {
    super(id);
    this.message = message;
    this.title = title;
    this.details = details;
    all().add(this);
  }
  
  public boolean isActivated() { return true; }
}
