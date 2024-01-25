package hudson.util;

import hudson.RestrictedSince;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.lang.ArrayUtils;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@RestrictedSince("2.236")
public class Protector {
  private static final String ALGORITHM_MODE = "AES/CBC/PKCS5Padding";
  
  private static final String ALGORITHM = "AES";
  
  private static final String MAGIC = ":::MAGIC";
  
  private static final int IV_BYTES = 16;
  
  private static final SecretKey KEY;
  
  public static String protect(String secret) {
    try {
      byte[] iv = new byte[16];
      SR.nextBytes(iv);
      Cipher cipher = Secret.getCipher("AES/CBC/PKCS5Padding");
      cipher.init(1, KEY, new IvParameterSpec(iv));
      byte[] encrypted = cipher.doFinal((secret + ":::MAGIC").getBytes(StandardCharsets.UTF_8));
      byte[] value = ArrayUtils.addAll(iv, encrypted);
      return new String(Base64.getEncoder().encode(value), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new Error(e);
    } 
  }
  
  public static String unprotect(String data) {
    if (data == null)
      return null; 
    try {
      byte[] value = Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8));
      byte[] iv = Arrays.copyOfRange(value, 0, 16);
      byte[] encrypted = Arrays.copyOfRange(value, 16, value.length);
      Cipher cipher = Secret.getCipher("AES/CBC/PKCS5Padding");
      cipher.init(2, KEY, new IvParameterSpec(iv));
      String plainText = new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
      if (plainText.endsWith(":::MAGIC"))
        return plainText.substring(0, plainText.length() - ":::MAGIC".length()); 
      return null;
    } catch (GeneralSecurityException|RuntimeException e) {
      return null;
    } 
  }
  
  private static final SecureRandom SR = new SecureRandom();
  
  static  {
    try {
      instance = KeyGenerator.getInstance("AES");
      KEY = instance.generateKey();
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    } 
  }
}
