package jenkins.fingerprints;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionList;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol({"fingerprints"})
public class GlobalFingerprintConfiguration extends GlobalConfiguration {
  private FingerprintStorage storage;
  
  private static final Logger LOGGER = Logger.getLogger(GlobalFingerprintConfiguration.class.getName());
  
  private boolean fingerprintCleanupDisabled;
  
  public GlobalFingerprintConfiguration() {
    this.storage = (FingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class);
    load();
  }
  
  public static GlobalFingerprintConfiguration get() { return (GlobalFingerprintConfiguration)ExtensionList.lookupSingleton(GlobalFingerprintConfiguration.class); }
  
  public FingerprintStorage getStorage() { return this.storage; }
  
  @DataBoundSetter
  public void setStorage(FingerprintStorage fingerprintStorage) {
    this.storage = fingerprintStorage;
    LOGGER.fine("Fingerprint Storage for the system changed to " + fingerprintStorage
        .getDescriptor().getDisplayName());
  }
  
  public boolean isFingerprintCleanupDisabled() { return this.fingerprintCleanupDisabled; }
  
  @DataBoundSetter
  public void setFingerprintCleanupDisabled(boolean fingerprintCleanupDisabled) { this.fingerprintCleanupDisabled = fingerprintCleanupDisabled; }
  
  public boolean configure(StaplerRequest req, JSONObject json) {
    req.bindJSON(this, json);
    save();
    return true;
  }
  
  public DescriptorExtensionList<FingerprintStorage, FingerprintStorageDescriptor> getFingerprintStorageDescriptors() { return FingerprintStorageDescriptor.all(); }
}
