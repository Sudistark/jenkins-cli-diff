package jenkins.security;

import hudson.Util;
import java.io.IOException;

public class HexStringConfidentialKey extends ConfidentialKey {
  private final int length;
  
  private ConfidentialStore lastCS;
  
  private String secret;
  
  public HexStringConfidentialKey(String id, int length) {
    super(id);
    if (length % 2 != 0)
      throw new IllegalArgumentException("length must be even: " + length); 
    this.length = length;
  }
  
  public HexStringConfidentialKey(Class owner, String shortName, int length) { this(owner.getName() + "." + owner.getName(), length); }
  
  public String get() {
    ConfidentialStore cs = ConfidentialStore.get();
    if (this.secret == null || cs != this.lastCS) {
      this.lastCS = cs;
      try {
        byte[] payload = load();
        if (payload == null) {
          payload = cs.randomBytes(this.length / 2);
          store(payload);
        } 
        this.secret = Util.toHexString(payload).substring(0, this.length);
      } catch (IOException e) {
        throw new Error("Failed to load the key: " + getId(), e);
      } 
    } 
    return this.secret;
  }
}
