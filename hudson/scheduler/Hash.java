package hudson.scheduler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public abstract class Hash {
  public abstract int next(int paramInt);
  
  @SuppressFBWarnings(value = {"PREDICTABLE_RANDOM"}, justification = "The random is just used for load distribution.")
  public static Hash from(String seed) {
    try {
      MessageDigest md5 = getMd5();
      md5.update(seed.getBytes(StandardCharsets.UTF_8));
      byte[] digest = md5.digest();
      for (int i = 8; i < digest.length; i++)
        digest[i % 8] = (byte)(digest[i % 8] ^ digest[i]); 
      long l = 0L;
      for (int i = 0; i < 8; i++)
        l = (l << 8) + (digest[i] & 0xFF); 
      Random rnd = new Random(l);
      return new Object(rnd);
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    } 
  }
  
  @SuppressFBWarnings(value = {"WEAK_MESSAGE_DIGEST_MD5"}, justification = "Should not be used for security.")
  private static MessageDigest getMd5() throws NoSuchAlgorithmException { return MessageDigest.getInstance("MD5"); }
  
  public static Hash zero() { return ZERO; }
  
  private static final Hash ZERO = new Object();
}
