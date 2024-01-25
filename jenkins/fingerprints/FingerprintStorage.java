package jenkins.fingerprints;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Fingerprint;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Date;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public abstract class FingerprintStorage extends AbstractDescribableImpl<FingerprintStorage> implements ExtensionPoint {
  public static FingerprintStorage get() { return ((GlobalFingerprintConfiguration)ExtensionList.lookupSingleton(GlobalFingerprintConfiguration.class)).getStorage(); }
  
  @Deprecated
  public static FingerprintStorage getFileFingerprintStorage() { return (FingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class); }
  
  public boolean cleanFingerprint(@NonNull Fingerprint fingerprint, TaskListener taskListener) {
    try {
      if (!fingerprint.isAlive() && fingerprint.getFacetBlockingDeletion() == null) {
        taskListener.getLogger().println("deleting obsolete " + fingerprint);
        Fingerprint.delete(fingerprint.getHashString());
        return true;
      } 
      if (!fingerprint.isAlive()) {
        FingerprintFacet deletionBlockerFacet = fingerprint.getFacetBlockingDeletion();
        taskListener.getLogger().println(deletionBlockerFacet.getClass().getName() + " created on " + deletionBlockerFacet.getClass().getName() + " blocked deletion of " + new Date(deletionBlockerFacet
              .getTimestamp()));
      } 
      fingerprint = getFingerprint(fingerprint);
      return fingerprint.trim();
    } catch (IOException e) {
      Functions.printStackTrace(e, taskListener.error("Failed to process " + fingerprint.getHashString()));
      return false;
    } 
  }
  
  protected Fingerprint getFingerprint(Fingerprint fp) throws IOException { return Jenkins.get()._getFingerprint(fp.getHashString()); }
  
  public FingerprintStorageDescriptor getDescriptor() { return (FingerprintStorageDescriptor)super.getDescriptor(); }
  
  public abstract void save(Fingerprint paramFingerprint) throws IOException;
  
  @CheckForNull
  public abstract Fingerprint load(String paramString) throws IOException;
  
  public abstract void delete(String paramString) throws IOException;
  
  public abstract boolean isReady();
  
  public abstract void iterateAndCleanupFingerprints(TaskListener paramTaskListener);
}
