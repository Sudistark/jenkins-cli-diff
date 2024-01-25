package hudson.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Scrambler {
  private static final Logger LOGGER = Logger.getLogger(Scrambler.class.getName());
  
  public static String scramble(String secret) {
    if (secret == null)
      return null; 
    return Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
  }
  
  public static String descramble(String scrambled) {
    if (scrambled == null)
      return null; 
    try {
      return new String(Base64.getDecoder().decode(scrambled.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      LOGGER.log(Level.WARNING, "Corrupted data", e);
      return "";
    } 
  }
}
