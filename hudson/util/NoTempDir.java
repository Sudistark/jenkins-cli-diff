package hudson.util;

import java.io.IOException;
import org.kohsuke.accmod.Restricted;

public class NoTempDir extends BootFailure {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final IOException exception;
  
  public NoTempDir(IOException exception) {
    super(exception);
    this.exception = exception;
  }
  
  public String getTempDir() { return System.getProperty("java.io.tmpdir"); }
}
