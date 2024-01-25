package jenkins.model.identity;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import org.jenkinsci.remoting.util.KeyUtils;

@Extension
public class IdentityRootAction implements UnprotectedRootAction {
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return null; }
  
  public String getUrlName() { return (InstanceIdentityProvider.RSA.getKeyPair() == null) ? null : "instance-identity"; }
  
  @CheckForNull
  public String getPublicKey() {
    RSAPublicKey key = (RSAPublicKey)InstanceIdentityProvider.RSA.getPublicKey();
    if (key == null)
      return null; 
    byte[] encoded = Base64.getEncoder().encode(key.getEncoded());
    int index = 0;
    StringBuilder buf = new StringBuilder(encoded.length + 20);
    while (index < encoded.length) {
      int len = Math.min(64, encoded.length - index);
      if (index > 0)
        buf.append("\n"); 
      buf.append(new String(encoded, index, len, StandardCharsets.UTF_8));
      index += len;
    } 
    return String.format("-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----%n", new Object[] { buf });
  }
  
  @NonNull
  public String getFingerprint() { return KeyUtils.fingerprint(InstanceIdentityProvider.RSA.getPublicKey()); }
}
