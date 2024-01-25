package hudson.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.remoting.SerializableOnlyOverRemoting;

public final class PollingResult implements SerializableOnlyOverRemoting {
  @CheckForNull
  public final SCMRevisionState baseline;
  
  @CheckForNull
  public final SCMRevisionState remote;
  
  @NonNull
  public final Change change;
  
  public PollingResult(@CheckForNull SCMRevisionState baseline, @CheckForNull SCMRevisionState remote, @NonNull Change change) {
    if (change == null)
      throw new IllegalArgumentException(); 
    this.baseline = baseline;
    this.remote = remote;
    this.change = change;
  }
  
  public PollingResult(@NonNull Change change) { this(null, null, change); }
  
  public boolean hasChanges() { return (this.change.ordinal() > Change.INSIGNIFICANT.ordinal()); }
  
  public static final PollingResult NO_CHANGES = new PollingResult(Change.NONE);
  
  public static final PollingResult SIGNIFICANT = new PollingResult(Change.SIGNIFICANT);
  
  public static final PollingResult BUILD_NOW = new PollingResult(Change.INCOMPARABLE);
  
  private static final long serialVersionUID = 1L;
}
