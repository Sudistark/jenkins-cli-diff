package jenkins.security;

import hudson.Util;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HMACConfidentialKey extends ConfidentialKey {
  private ConfidentialStore lastCS;
  
  private SecretKey key;
  
  private Mac mac;
  
  private final int length;
  
  private static final String ALGORITHM = "HmacSHA256";
  
  public HMACConfidentialKey(String id, int length) {
    super(id);
    this.length = length;
  }
  
  public HMACConfidentialKey(String id) { this(id, 2147483647); }
  
  public HMACConfidentialKey(Class owner, String shortName, int length) { this(owner.getName() + "." + owner.getName(), length); }
  
  public HMACConfidentialKey(Class owner, String shortName) { this(owner, shortName, 2147483647); }
  
  public byte[] mac(byte[] message) {
    ConfidentialStore cs = ConfidentialStore.get();
    if (this.mac == null || cs != this.lastCS) {
      this.lastCS = cs;
      this.mac = createMac();
    } 
    return chop(this.mac.doFinal(message));
  }
  
  public boolean checkMac(byte[] message, byte[] mac) { return MessageDigest.isEqual(mac(message), mac); }
  
  public String mac(String message) { return Util.toHexString(mac(message.getBytes(StandardCharsets.UTF_8))); }
  
  public boolean checkMac(String message, String mac) { return MessageDigest.isEqual(mac(message).getBytes(StandardCharsets.UTF_8), mac.getBytes(StandardCharsets.UTF_8)); }
  
  private byte[] chop(byte[] mac) {
    if (mac.length <= this.length)
      return mac; 
    byte[] b = new byte[this.length];
    System.arraycopy(mac, 0, b, 0, b.length);
    return b;
  }
  
  public Mac createMac() {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(getKey());
      return mac;
    } catch (GeneralSecurityException e) {
      throw new Error("HmacSHA256 not supported?", e);
    } 
  }
  
  private SecretKey getKey() {
    ConfidentialStore cs = ConfidentialStore.get();
    if (this.key == null || cs != this.lastCS) {
      this.lastCS = cs;
      try {
        byte[] encoded = load();
        if (encoded == null) {
          KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256");
          SecretKey key = kg.generateKey();
          store(encoded = key.getEncoded());
        } 
        this.key = new SecretKeySpec(encoded, "HmacSHA256");
      } catch (IOException|java.security.NoSuchAlgorithmException e) {
        throw new Error("Failed to load the key: " + getId(), e);
      } 
    } 
    return this.key;
  }
}
