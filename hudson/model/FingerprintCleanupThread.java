package hudson.model;

import hudson.Extension;
import hudson.ExtensionList;
import java.util.logging.Logger;
import jenkins.fingerprints.FileFingerprintStorage;
import jenkins.fingerprints.FingerprintStorage;
import jenkins.fingerprints.GlobalFingerprintConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Symbol({"fingerprintCleanup"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class FingerprintCleanupThread extends AsyncPeriodicWork {
  private static final Logger LOGGER = Logger.getLogger(FingerprintCleanupThread.class.getName());
  
  public FingerprintCleanupThread() { super("Fingerprint cleanup"); }
  
  public long getRecurrencePeriod() { return 86400000L; }
  
  public static void invoke() { getInstance().run(); }
  
  private static FingerprintCleanupThread getInstance() { return (FingerprintCleanupThread)ExtensionList.lookup(AsyncPeriodicWork.class).get(FingerprintCleanupThread.class); }
  
  public void execute(TaskListener listener) {
    if (GlobalFingerprintConfiguration.get().isFingerprintCleanupDisabled()) {
      LOGGER.fine("Fingerprint cleanup is disabled. Skipping execution");
      return;
    } 
    FingerprintStorage.get().iterateAndCleanupFingerprints(listener);
    FileFingerprintStorage fileFingerprintStorage = (FileFingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class);
    if (!(FingerprintStorage.get() instanceof FileFingerprintStorage) && fileFingerprintStorage
      .isReady())
      fileFingerprintStorage.iterateAndCleanupFingerprints(listener); 
  }
}
