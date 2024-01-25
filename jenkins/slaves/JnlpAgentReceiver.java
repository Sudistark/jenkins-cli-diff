package jenkins.slaves;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import java.security.SecureRandom;
import jenkins.security.HMACConfidentialKey;
import org.jenkinsci.remoting.engine.JnlpClientDatabase;
import org.jenkinsci.remoting.engine.JnlpConnectionStateListener;

public abstract class JnlpAgentReceiver extends JnlpConnectionStateListener implements ExtensionPoint {
  public static final HMACConfidentialKey SLAVE_SECRET = new HMACConfidentialKey(JnlpSlaveAgentProtocol.class, "secret");
  
  private static final SecureRandom secureRandom = new SecureRandom();
  
  public static final JnlpClientDatabase DATABASE = new JnlpAgentDatabase();
  
  public static ExtensionList<JnlpAgentReceiver> all() { return ExtensionList.lookup(JnlpAgentReceiver.class); }
  
  public static boolean exists(String clientName) {
    for (JnlpAgentReceiver receiver : all()) {
      if (receiver.owns(clientName))
        return true; 
    } 
    return false;
  }
  
  protected abstract boolean owns(String paramString);
  
  public static String generateCookie() {
    cookie = new byte[32];
    secureRandom.nextBytes(cookie);
    return Util.toHexString(cookie);
  }
}
