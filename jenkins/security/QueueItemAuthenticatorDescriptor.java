package jenkins.security;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class QueueItemAuthenticatorDescriptor extends Descriptor<QueueItemAuthenticator> {
  public static DescriptorExtensionList<QueueItemAuthenticator, QueueItemAuthenticatorDescriptor> all() { return Jenkins.get().getDescriptorList(QueueItemAuthenticator.class); }
}
