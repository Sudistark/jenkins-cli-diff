package hudson.util;

import org.kohsuke.accmod.Restricted;

public class InsufficientPermissionDetected extends BootFailure {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final SecurityException exception;
  
  public InsufficientPermissionDetected(SecurityException e) {
    super(e);
    this.exception = e;
  }
}
