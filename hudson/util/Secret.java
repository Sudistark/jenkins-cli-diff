package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import jenkins.security.CryptoConfidentialKey;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;

public final class Secret implements Serializable {
  private static final Logger LOGGER = Logger.getLogger(Secret.class.getName());
  
  private static final byte PAYLOAD_V1 = 1;
  
  @NonNull
  private final String value;
  
  private byte[] iv;
  
  Secret(String value) { this.value = value; }
  
  Secret(String value, byte[] iv) {
    this.value = value;
    this.iv = iv;
  }
  
  @Deprecated
  public String toString() {
    String from = (new Throwable()).getStackTrace()[1].toString();
    LOGGER.warning("Use of toString() on hudson.util.Secret from " + from + ". Prefer getPlainText() or getEncryptedValue() depending your needs. see https://www.jenkins.io/redirect/hudson.util.Secret/");
    return this.value;
  }
  
  @NonNull
  public String getPlainText() { return this.value; }
  
  public boolean equals(Object that) { return (that instanceof Secret && this.value.equals(((Secret)that).value)); }
  
  public int hashCode() { return this.value.hashCode(); }
  
  public String getEncryptedValue() {
    try {
      synchronized (this) {
        if (this.iv == null)
          this.iv = KEY.newIv(); 
      } 
      Cipher cipher = KEY.encrypt(this.iv);
      byte[] encrypted = cipher.doFinal(this.value.getBytes(StandardCharsets.UTF_8));
      byte[] payload = new byte[9 + this.iv.length + encrypted.length];
      int pos = 0;
      payload[pos++] = 1;
      payload[pos++] = (byte)(this.iv.length >> 24);
      payload[pos++] = (byte)(this.iv.length >> 16);
      payload[pos++] = (byte)(this.iv.length >> 8);
      payload[pos++] = (byte)this.iv.length;
      payload[pos++] = (byte)(encrypted.length >> 24);
      payload[pos++] = (byte)(encrypted.length >> 16);
      payload[pos++] = (byte)(encrypted.length >> 8);
      payload[pos++] = (byte)encrypted.length;
      System.arraycopy(this.iv, 0, payload, pos, this.iv.length);
      pos += this.iv.length;
      System.arraycopy(encrypted, 0, payload, pos, encrypted.length);
      return "{" + Base64.getEncoder().encodeToString(payload) + "}";
    } catch (GeneralSecurityException e) {
      throw new Error(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final Pattern ENCRYPTED_VALUE_PATTERN = Pattern.compile("\\{?[A-Za-z0-9+/]+={0,2}}?");
  
  @CheckForNull
  public static Secret decrypt(@CheckForNull String data) {
    if (!isValidData(data))
      return null; 
    if (data.startsWith("{") && data.endsWith("}")) {
      String text;
      byte[] code;
      byte[] iv;
      int dataLength;
      int ivLength;
      byte[] payload;
      try {
        payload = Base64.getDecoder().decode(data.substring(1, data.length() - 1));
      } catch (IllegalArgumentException e) {
        return null;
      } 
      switch (payload[0]) {
        case 1:
          ivLength = (payload[1] & 0xFF) << 24 | (payload[2] & 0xFF) << 16 | (payload[3] & 0xFF) << 8 | payload[4] & 0xFF;
          dataLength = (payload[5] & 0xFF) << 24 | (payload[6] & 0xFF) << 16 | (payload[7] & 0xFF) << 8 | payload[8] & 0xFF;
          if (payload.length != 9 + ivLength + dataLength)
            return null; 
          iv = Arrays.copyOfRange(payload, 9, 9 + ivLength);
          code = Arrays.copyOfRange(payload, 9 + ivLength, payload.length);
          try {
            text = new String(KEY.decrypt(iv).doFinal(code), StandardCharsets.UTF_8);
          } catch (GeneralSecurityException e) {
            return null;
          } 
          return new Secret(text, iv);
      } 
      return null;
    } 
    try {
      return HistoricalSecrets.decrypt(data, KEY);
    } catch (UnsupportedEncodingException e) {
      byte[] payload;
      throw new Error(payload);
    } catch (GeneralSecurityException|java.io.IOException e) {
      return null;
    } 
  }
  
  private static boolean isValidData(String data) {
    if (data == null || "{}".equals(data) || "".equals(data.trim()))
      return false; 
    if (data.startsWith("{") && data.endsWith("}"))
      return !"".equals(data.substring(1, data.length() - 1).trim()); 
    return true;
  }
  
  public static Cipher getCipher(String algorithm) throws GeneralSecurityException {
    return (PROVIDER != null) ? Cipher.getInstance(algorithm, PROVIDER) : 
      Cipher.getInstance(algorithm);
  }
  
  @NonNull
  public static Secret fromString(@CheckForNull String data) {
    data = Util.fixNull(data);
    Secret s = decrypt(data);
    if (s == null)
      s = new Secret(data); 
    return s;
  }
  
  @NonNull
  public static String toString(@CheckForNull Secret s) { return (s == null) ? "" : s.value; }
  
  private static final String PROVIDER = SystemProperties.getString(Secret.class.getName() + ".provider");
  
  @Deprecated
  static String SECRET = null;
  
  private static final CryptoConfidentialKey KEY = new CryptoConfidentialKey(Secret.class.getName());
  
  private static final long serialVersionUID = 1L;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final boolean AUTO_ENCRYPT_PASSWORD_CONTROL = SystemProperties.getBoolean(Secret.class.getName() + ".AUTO_ENCRYPT_PASSWORD_CONTROL", true);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean BLANK_NONSECRET_PASSWORD_FIELDS_WITHOUT_ITEM_CONFIGURE = SystemProperties.getBoolean(Secret.class.getName() + ".BLANK_NONSECRET_PASSWORD_FIELDS_WITHOUT_ITEM_CONFIGURE", true);
  
  static  {
    Stapler.CONVERT_UTILS.register(new Object(), Secret.class);
    if (AUTO_ENCRYPT_PASSWORD_CONTROL)
      Stapler.CONVERT_UTILS.register(new Object(), String.class); 
  }
}
