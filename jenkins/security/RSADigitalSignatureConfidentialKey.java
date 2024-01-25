package jenkins.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

public class RSADigitalSignatureConfidentialKey extends RSAConfidentialKey {
  static final String SIGNING_ALGORITHM = "SHA256";
  
  public RSADigitalSignatureConfidentialKey(String id) { super(id); }
  
  public RSADigitalSignatureConfidentialKey(Class owner, String shortName) { super(owner, shortName); }
  
  public String sign(String msg) {
    try {
      RSAPrivateKey key = getPrivateKey();
      Signature sig = Signature.getInstance("SHA256with" + key.getAlgorithm());
      sig.initSign(key);
      sig.update(msg.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(sig.sign());
    } catch (GeneralSecurityException e) {
      throw new SecurityException(e);
    } 
  }
}
