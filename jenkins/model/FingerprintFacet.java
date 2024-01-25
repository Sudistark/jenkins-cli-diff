package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Action;
import hudson.model.Fingerprint;
import java.util.List;
import org.kohsuke.accmod.Restricted;

public abstract class FingerprintFacet implements ExtensionPoint {
  private Fingerprint fingerprint;
  
  private final long timestamp;
  
  protected FingerprintFacet(@NonNull Fingerprint fingerprint, long timestamp) {
    assert fingerprint != null;
    this.fingerprint = fingerprint;
    this.timestamp = timestamp;
  }
  
  @NonNull
  public Fingerprint getFingerprint() { return this.fingerprint; }
  
  public void createActions(List<Action> result) {}
  
  public long getTimestamp() { return this.timestamp; }
  
  public boolean isFingerprintDeletionBlocked() { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void _setOwner(Fingerprint fingerprint) {
    assert fingerprint != null;
    this.fingerprint = fingerprint;
  }
}
