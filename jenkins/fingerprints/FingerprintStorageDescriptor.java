package jenkins.fingerprints;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public class FingerprintStorageDescriptor extends Descriptor<FingerprintStorage> {
  public static DescriptorExtensionList<FingerprintStorage, FingerprintStorageDescriptor> all() { return Jenkins.get().getDescriptorList(FingerprintStorage.class); }
}
