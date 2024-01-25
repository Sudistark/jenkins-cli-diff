package hudson.util;

import java.util.Map;

public class IncompatibleVMDetected extends BootFailure {
  public Map getSystemProperties() { return System.getProperties(); }
}
