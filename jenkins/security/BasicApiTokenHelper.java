package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import jenkins.model.GlobalConfiguration;
import jenkins.security.apitoken.ApiTokenPropertyConfiguration;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class BasicApiTokenHelper {
  @CheckForNull
  public static User isConnectingUsingApiToken(String username, String tokenValue) {
    User user = User.getById(username, false);
    if (user == null) {
      ApiTokenPropertyConfiguration apiTokenConfiguration = (ApiTokenPropertyConfiguration)GlobalConfiguration.all().getInstance(ApiTokenPropertyConfiguration.class);
      if (apiTokenConfiguration.isTokenGenerationOnCreationEnabled()) {
        String generatedTokenOnCreation = Util.getDigestOf(ApiTokenProperty.API_KEY_SEED.mac(username));
        boolean areTokenEqual = MessageDigest.isEqual(generatedTokenOnCreation
            .getBytes(StandardCharsets.US_ASCII), tokenValue
            .getBytes(StandardCharsets.US_ASCII));
        if (areTokenEqual)
          return User.getById(username, true); 
      } 
    } else {
      ApiTokenProperty t = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
      if (t != null && t.matchesPassword(tokenValue))
        return user; 
    } 
    return null;
  }
}
