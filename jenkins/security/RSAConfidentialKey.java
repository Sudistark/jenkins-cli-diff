package jenkins.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public abstract class RSAConfidentialKey extends ConfidentialKey {
  private ConfidentialStore lastCS;
  
  private RSAPrivateKey priv;
  
  private RSAPublicKey pub;
  
  protected RSAConfidentialKey(String id) { super(id); }
  
  protected RSAConfidentialKey(Class owner, String shortName) { this(owner.getName() + "." + owner.getName()); }
  
  protected RSAPrivateKey getPrivateKey() {
    try {
      ConfidentialStore cs = ConfidentialStore.get();
      if (this.priv == null || cs != this.lastCS) {
        this.lastCS = cs;
        byte[] payload = load();
        if (payload == null) {
          KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
          gen.initialize(2048, cs.secureRandom());
          KeyPair keys = gen.generateKeyPair();
          this.priv = (RSAPrivateKey)keys.getPrivate();
          this.pub = (RSAPublicKey)keys.getPublic();
          store(this.priv.getEncoded());
        } else {
          KeyFactory keyFactory = KeyFactory.getInstance("RSA");
          this.priv = (RSAPrivateKey)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(payload));
          RSAPrivateCrtKey pks = (RSAPrivateCrtKey)this.priv;
          this.pub = (RSAPublicKey)keyFactory.generatePublic(new RSAPublicKeySpec(pks
                .getModulus(), pks.getPublicExponent()));
        } 
      } 
      return this.priv;
    } catch (IOException|java.security.GeneralSecurityException e) {
      throw new Error("Failed to load the key: " + getId(), e);
    } 
  }
  
  public RSAPublicKey getPublicKey() {
    getPrivateKey();
    return this.pub;
  }
  
  public String getEncodedPublicKey() { return Base64.getEncoder().encodeToString(getPublicKey().getEncoded()); }
}
