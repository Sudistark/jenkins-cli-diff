package hudson.node_monitors;

import hudson.model.Computer;
import java.text.ParseException;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

public abstract class AbstractDiskSpaceMonitor extends NodeMonitor {
  public final String freeSpaceThreshold;
  
  protected AbstractDiskSpaceMonitor(String threshold) throws ParseException {
    this.freeSpaceThreshold = threshold;
    DiskSpaceMonitorDescriptor.DiskSpace.parse(threshold);
  }
  
  protected AbstractDiskSpaceMonitor() { this.freeSpaceThreshold = "1GB"; }
  
  public long getThresholdBytes() {
    if (this.freeSpaceThreshold == null)
      return 1073741824L; 
    try {
      return (DiskSpaceMonitorDescriptor.DiskSpace.parse(this.freeSpaceThreshold)).size;
    } catch (ParseException e) {
      return 1073741824L;
    } 
  }
  
  public Object data(Computer c) {
    DiskSpaceMonitorDescriptor.DiskSpace size = markNodeOfflineIfDiskspaceIsTooLow(c);
    if (size != null && size.size > getThresholdBytes() && c.isOffline() && c.getOfflineCause() instanceof DiskSpaceMonitorDescriptor.DiskSpace && 
      getClass().equals(((DiskSpaceMonitorDescriptor.DiskSpace)c.getOfflineCause()).getTrigger()) && 
      getDescriptor().markOnline(c))
      LOGGER.info(Messages.DiskSpaceMonitor_MarkedOnline(c.getDisplayName())); 
    return size;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public DiskSpaceMonitorDescriptor.DiskSpace markNodeOfflineIfDiskspaceIsTooLow(Computer c) {
    DiskSpaceMonitorDescriptor.DiskSpace size = (DiskSpaceMonitorDescriptor.DiskSpace)super.data(c);
    if (size != null && size.size < getThresholdBytes()) {
      size.setTriggered(getClass(), true);
      if (getDescriptor().markOffline(c, size))
        LOGGER.warning(Messages.DiskSpaceMonitor_MarkedOffline(c.getDisplayName())); 
    } 
    return size;
  }
  
  private static final Logger LOGGER = Logger.getLogger(AbstractDiskSpaceMonitor.class.getName());
  
  private static final long DEFAULT_THRESHOLD = 1073741824L;
}
