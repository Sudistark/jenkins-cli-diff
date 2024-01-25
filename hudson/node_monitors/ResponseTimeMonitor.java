package hudson.node_monitors;

import hudson.Extension;
import java.util.logging.Logger;

public class ResponseTimeMonitor extends NodeMonitor {
  @Extension
  public static final AbstractNodeMonitorDescriptor<Data> DESCRIPTOR = new Object();
  
  private static final long TIMEOUT = 5000L;
  
  private static final Logger LOGGER = Logger.getLogger(ResponseTimeMonitor.class.getName());
}
