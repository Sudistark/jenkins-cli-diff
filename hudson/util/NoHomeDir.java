package hudson.util;

import java.io.File;

public class NoHomeDir extends BootFailure {
  public final File home;
  
  public NoHomeDir(File home) { this.home = home; }
}
