package jenkins.security;

import hudson.util.Secret;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.kohsuke.accmod.Restricted;

public class CryptoConfidentialKey extends ConfidentialKey {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final int DEFAULT_IV_LENGTH = 16;
  
  private ConfidentialStore lastCS;
  
  private SecretKey secret;
  
  private static final String KEY_ALGORITHM = "AES";
  
  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  
  public CryptoConfidentialKey(String id) { super(id); }
  
  public CryptoConfidentialKey(Class owner, String shortName) { this(owner.getName() + "." + owner.getName()); }
  
  private SecretKey getKey() {
    ConfidentialStore cs = ConfidentialStore.get();
    if (this.secret == null || cs != this.lastCS) {
      this.lastCS = cs;
      try {
        byte[] payload = load();
        if (payload == null) {
          payload = cs.randomBytes(256);
          store(payload);
        } 
        this.secret = new SecretKeySpec(payload, 0, 16, "AES");
      } catch (IOException e) {
        throw new Error("Failed to load the key: " + getId(), e);
      } 
    } 
    return this.secret;
  }
  
  @Deprecated
  public Cipher encrypt() {
    try {
      Cipher cipher = Secret.getCipher("AES");
      cipher.init(1, getKey());
      return cipher;
    } catch (GeneralSecurityException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Cipher encrypt(byte[] iv) {
    try {
      Cipher cipher = Secret.getCipher("AES/CBC/PKCS5Padding");
      cipher.init(1, getKey(), new IvParameterSpec(iv));
      return cipher;
    } catch (GeneralSecurityException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Cipher decrypt(byte[] iv) {
    try {
      Cipher cipher = Secret.getCipher("AES/CBC/PKCS5Padding");
      cipher.init(2, getKey(), new IvParameterSpec(iv));
      return cipher;
    } catch (GeneralSecurityException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public byte[] newIv(int length) { return ConfidentialStore.get().randomBytes(length); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public byte[] newIv() { return newIv(16); }
  
  @Deprecated
  public Cipher decrypt() {
    try {
      Cipher cipher = Secret.getCipher("AES");
      cipher.init(2, getKey());
      return cipher;
    } catch (GeneralSecurityException e) {
      throw new AssertionError(e);
    } 
  }
}
