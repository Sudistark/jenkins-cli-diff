package hudson.node_monitors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.util.ClockDifference;
import org.kohsuke.accmod.Restricted;

public class ClockMonitor extends NodeMonitor {
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_PKGPROTECT"}, justification = "for backward compatibility")
  public static AbstractNodeMonitorDescriptor<ClockDifference> DESCRIPTOR;
  
  public ClockDifference getDifferenceFor(Computer c) { return (ClockDifference)DESCRIPTOR.get(c); }
}
