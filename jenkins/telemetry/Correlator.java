package jenkins.telemetry;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.UUID;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Correlator extends Descriptor<Correlator> implements Describable<Correlator> {
  private String correlationId;
  
  public Correlator() {
    super(Correlator.class);
    load();
    if (this.correlationId == null) {
      this.correlationId = UUID.randomUUID().toString();
      save();
    } 
  }
  
  public String getCorrelationId() { return this.correlationId; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
  
  public Descriptor<Correlator> getDescriptor() { return this; }
}
