package hudson.util;

import org.kohsuke.accmod.Restricted;

public class HudsonFailedToLoad extends BootFailure {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final Throwable exception;
  
  public HudsonFailedToLoad(Throwable exception) {
    super(exception);
    this.exception = exception;
  }
}
