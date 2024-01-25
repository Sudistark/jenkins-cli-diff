package jenkins.security.seed;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.UserProperty;
import java.security.SecureRandom;
import java.util.Objects;
import jenkins.util.SystemProperties;
import org.apache.commons.codec.binary.Hex;
import org.kohsuke.accmod.Restricted;

public class UserSeedProperty extends UserProperty {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLE_USER_SEED = SystemProperties.getBoolean(UserSeedProperty.class.getName() + ".disableUserSeed");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean HIDE_USER_SEED_SECTION = SystemProperties.getBoolean(UserSeedProperty.class.getName() + ".hideUserSeedSection");
  
  public static final String USER_SESSION_SEED = "_JENKINS_SESSION_SEED";
  
  private static final SecureRandom RANDOM = new SecureRandom();
  
  private static final int SEED_NUM_BYTES = 8;
  
  private String seed;
  
  private UserSeedProperty() { renewSeedInternal(); }
  
  @NonNull
  public String getSeed() { return this.seed; }
  
  public void renewSeed() {
    renewSeedInternal();
    UserSeedChangeListener.fireUserSeedRenewed(this.user);
  }
  
  private void renewSeedInternal() {
    String currentSeed = this.seed;
    String newSeed = currentSeed;
    byte[] bytes = new byte[8];
    while (Objects.equals(newSeed, currentSeed)) {
      RANDOM.nextBytes(bytes);
      newSeed = new String(Hex.encodeHex(bytes));
    } 
    this.seed = newSeed;
  }
}
