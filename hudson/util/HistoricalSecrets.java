package hudson.util;

import hudson.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import jenkins.model.Jenkins;
import jenkins.security.CryptoConfidentialKey;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class HistoricalSecrets {
  static final String MAGIC = "::::MAGIC::::";
  
  static Secret decrypt(String data, CryptoConfidentialKey key) throws IOException, GeneralSecurityException {
    byte[] in;
    try {
      in = Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
    } catch (IllegalArgumentException ex) {
      throw new IOException("Could not decode secret", ex);
    } 
    Secret s = tryDecrypt(key.decrypt(), in);
    if (s != null)
      return s; 
    Cipher cipher = Secret.getCipher("AES");
    cipher.init(2, getLegacyKey());
    return tryDecrypt(cipher, in);
  }
  
  static Secret tryDecrypt(Cipher cipher, byte[] in) {
    try {
      String plainText = new String(cipher.doFinal(in), StandardCharsets.UTF_8);
      if (plainText.endsWith("::::MAGIC::::"))
        return new Secret(plainText.substring(0, plainText.length() - "::::MAGIC::::".length())); 
      return null;
    } catch (GeneralSecurityException e) {
      return null;
    } 
  }
  
  @Deprecated
  static SecretKey getLegacyKey() throws GeneralSecurityException {
    if (Secret.SECRET != null)
      return Util.toAes128Key(Secret.SECRET); 
    j = Jenkins.getInstanceOrNull();
    if (j != null)
      return j.getSecretKeyAsAES128(); 
    return Util.toAes128Key("mock");
  }
}
