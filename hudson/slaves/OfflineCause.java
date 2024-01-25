package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Date;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class OfflineCause {
  protected final long timestamp = System.currentTimeMillis();
  
  @Exported
  public long getTimestamp() { return this.timestamp; }
  
  @NonNull
  public final Date getTime() { return new Date(this.timestamp); }
  
  public static OfflineCause create(Localizable d) {
    if (d == null)
      return null; 
    return new SimpleOfflineCause(d);
  }
}
