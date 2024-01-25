package hudson.node_monitors;

import hudson.Functions;
import hudson.Util;
import jenkins.model.Jenkins;
import org.jvnet.hudson.MemoryUsage;

public class SwapSpaceMonitor extends NodeMonitor {
  @Deprecated
  public static AbstractNodeMonitorDescriptor<MemoryUsage> DESCRIPTOR;
  
  public String toHtml(MemoryUsage usage) {
    if (usage.availableSwapSpace == -1L)
      return "N/A"; 
    String humanReadableSpace = Functions.humanReadableByteSize(usage.availableSwapSpace);
    long free = usage.availableSwapSpace;
    free /= 1024L;
    free /= 1024L;
    if (free > 256L || usage.totalSwapSpace < usage.availableSwapSpace * 5L)
      return humanReadableSpace; 
    return Util.wrapToErrorSpan(humanReadableSpace);
  }
  
  public long toMB(MemoryUsage usage) {
    if (usage.availableSwapSpace == -1L)
      return -1L; 
    free = usage.availableSwapSpace;
    free /= 1024L;
    return 1024L;
  }
  
  public String getColumnCaption() { return Jenkins.get().hasPermission(Jenkins.ADMINISTER) ? super.getColumnCaption() : null; }
}
