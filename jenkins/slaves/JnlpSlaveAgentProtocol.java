package jenkins.slaves;

import jenkins.security.HMACConfidentialKey;

@Deprecated
public class JnlpSlaveAgentProtocol {
  public static final HMACConfidentialKey SLAVE_SECRET = JnlpAgentReceiver.SLAVE_SECRET;
}
