package jenkins.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Fingerprint;
import java.util.List;

public abstract class TransientFingerprintFacetFactory implements ExtensionPoint {
  public abstract void createFor(Fingerprint paramFingerprint, List<FingerprintFacet> paramList);
  
  public static ExtensionList<TransientFingerprintFacetFactory> all() { return ExtensionList.lookup(TransientFingerprintFacetFactory.class); }
}
