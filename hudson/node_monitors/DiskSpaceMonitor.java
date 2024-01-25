package hudson.node_monitors;

import hudson.Extension;
import hudson.model.Computer;
import java.text.ParseException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public class DiskSpaceMonitor extends AbstractDiskSpaceMonitor {
  @DataBoundConstructor
  public DiskSpaceMonitor(String freeSpaceThreshold) throws ParseException { super(freeSpaceThreshold); }
  
  public DiskSpaceMonitor() {}
  
  public DiskSpaceMonitorDescriptor.DiskSpace getFreeSpace(Computer c) { return (DiskSpaceMonitorDescriptor.DiskSpace)DESCRIPTOR.get(c); }
  
  public String getColumnCaption() { return Jenkins.get().hasPermission(Jenkins.ADMINISTER) ? super.getColumnCaption() : null; }
  
  public static final DiskSpaceMonitorDescriptor DESCRIPTOR = new Object();
  
  @Extension
  public static DiskSpaceMonitorDescriptor install() { return DESCRIPTOR; }
}
