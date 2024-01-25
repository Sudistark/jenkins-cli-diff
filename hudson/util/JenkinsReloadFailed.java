package hudson.util;

import org.kohsuke.accmod.Restricted;

public class JenkinsReloadFailed extends BootFailure {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final Throwable cause;
  
  public JenkinsReloadFailed(Throwable cause) {
    super(cause);
    this.cause = cause;
  }
}
