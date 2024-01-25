package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.queue.MappingWorksheet;
import java.util.logging.Logger;

public abstract class LoadBalancer implements ExtensionPoint {
  public static final LoadBalancer CONSISTENT_HASH = new Object();
  
  @Deprecated
  public static final LoadBalancer DEFAULT = CONSISTENT_HASH;
  
  @CheckForNull
  public abstract MappingWorksheet.Mapping map(@NonNull Queue.Task paramTask, MappingWorksheet paramMappingWorksheet);
  
  protected LoadBalancer sanitize() {
    LoadBalancer base = this;
    return new Object(this, base);
  }
  
  private static final Logger LOGGER = Logger.getLogger(LoadBalancer.class.getName());
}
