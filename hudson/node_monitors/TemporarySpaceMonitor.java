package hudson.node_monitors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import java.text.ParseException;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

public class TemporarySpaceMonitor extends AbstractDiskSpaceMonitor {
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_PKGPROTECT"}, justification = "for backward compatibility")
  public static DiskSpaceMonitorDescriptor DESCRIPTOR;
  
  @DataBoundConstructor
  public TemporarySpaceMonitor(String freeSpaceThreshold) throws ParseException { super(freeSpaceThreshold); }
  
  public TemporarySpaceMonitor() {}
  
  public DiskSpaceMonitorDescriptor.DiskSpace getFreeSpace(Computer c) {
    DiskSpaceMonitorDescriptor descriptor = (DiskSpaceMonitorDescriptor)Jenkins.get().getDescriptor(TemporarySpaceMonitor.class);
    return (descriptor != null) ? (DiskSpaceMonitorDescriptor.DiskSpace)descriptor.get(c) : null;
  }
  
  public String getColumnCaption() { return Jenkins.get().hasPermission(Jenkins.ADMINISTER) ? super.getColumnCaption() : null; }
  
  @Deprecated
  public static DiskSpaceMonitorDescriptor install() { return (DiskSpaceMonitorDescriptor)Jenkins.get().getDescriptor(TemporarySpaceMonitor.class); }
}
