package hudson.util;

import org.kohsuke.accmod.Restricted;

public class AWTProblem extends BootFailure {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final Throwable cause;
  
  public AWTProblem(Throwable cause) {
    super(cause);
    this.cause = cause;
  }
}
