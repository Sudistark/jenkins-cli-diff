package hudson.node_monitors;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.slaves.OfflineCause;

public abstract class MonitorOfflineCause extends OfflineCause {
  @NonNull
  public abstract Class<? extends NodeMonitor> getTrigger();
}
