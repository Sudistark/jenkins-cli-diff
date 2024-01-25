package jenkins.security.apitoken;

import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public class TokenUuidAndPlainValue {
  public final String tokenUuid;
  
  public final String plainValue;
  
  public TokenUuidAndPlainValue(String tokenUuid, String plainValue) {
    this.tokenUuid = tokenUuid;
    this.plainValue = plainValue;
  }
}
