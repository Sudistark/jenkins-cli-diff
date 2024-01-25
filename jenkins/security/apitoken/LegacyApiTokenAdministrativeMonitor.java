package jenkins.security.apitoken;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.model.User;
import hudson.util.HttpResponses;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.security.ApiTokenProperty;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.json.JsonBody;

@Extension
@Symbol({"legacyApiTokenUsage"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class LegacyApiTokenAdministrativeMonitor extends AdministrativeMonitor {
  private static final Logger LOGGER = Logger.getLogger(LegacyApiTokenAdministrativeMonitor.class.getName());
  
  public LegacyApiTokenAdministrativeMonitor() { super("legacyApiToken"); }
  
  public String getDisplayName() { return Messages.LegacyApiTokenAdministrativeMonitor_displayName(); }
  
  public boolean isActivated() {
    return User.getAll().stream()
      .anyMatch(user -> {
          ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
          return (apiTokenProperty != null && apiTokenProperty.hasLegacyToken());
        });
  }
  
  public boolean isSecurity() { return true; }
  
  public HttpResponse doIndex() throws IOException { return new HttpRedirect("manage"); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<User> getImpactedUserList() {
    return (List)User.getAll().stream()
      .filter(user -> {
          ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
          return (apiTokenProperty != null && apiTokenProperty.hasLegacyToken());
        }).collect(Collectors.toList());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Nullable
  public ApiTokenStore.HashedToken getLegacyTokenOf(@NonNull User user) {
    ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
    return apiTokenProperty.getTokenStore().getLegacyToken();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Nullable
  public ApiTokenProperty.TokenInfoAndStats getLegacyStatsOf(@NonNull User user, ApiTokenStore.HashedToken legacyToken) {
    ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
    if (legacyToken != null) {
      ApiTokenStats.SingleTokenStats legacyStats = apiTokenProperty.getTokenStats().findTokenStatsById(legacyToken.getUuid());
      return new ApiTokenProperty.TokenInfoAndStats(legacyToken, legacyStats);
    } 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasFreshToken(@NonNull User user, ApiTokenProperty.TokenInfoAndStats legacyStats) {
    if (legacyStats == null)
      return false; 
    ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
    return apiTokenProperty.getTokenList().stream()
      .filter(token -> !token.isLegacy)
      .anyMatch(token -> {
          Date creationDate = token.creationDate;
          Date lastUseDate = legacyStats.lastUseDate;
          if (lastUseDate == null)
            lastUseDate = legacyStats.creationDate; 
          return (creationDate != null && lastUseDate != null && creationDate.after(lastUseDate));
        });
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasMoreRecentlyUsedToken(@NonNull User user, ApiTokenProperty.TokenInfoAndStats legacyStats) {
    if (legacyStats == null)
      return false; 
    ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
    return apiTokenProperty.getTokenList().stream()
      .filter(token -> !token.isLegacy)
      .anyMatch(token -> {
          Date currentLastUseDate = token.lastUseDate;
          Date legacyLastUseDate = legacyStats.lastUseDate;
          if (legacyLastUseDate == null)
            legacyLastUseDate = legacyStats.creationDate; 
          return (currentLastUseDate != null && legacyLastUseDate != null && currentLastUseDate.after(legacyLastUseDate));
        });
  }
  
  @RequirePOST
  @SuppressFBWarnings(value = {"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"}, justification = "written to by Stapler")
  public HttpResponse doRevokeAllSelected(@JsonBody RevokeAllSelectedModel content) throws IOException {
    for (RevokeAllSelectedUserAndUuid value : content.values) {
      if (value.userId == null)
        value.userId = "null"; 
      User user = User.getById(value.userId, false);
      if (user == null) {
        LOGGER.log(Level.INFO, "User not found id={0}", value.userId);
      } else {
        ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
        if (apiTokenProperty == null) {
          LOGGER.log(Level.INFO, "User without apiTokenProperty found id={0}", value.userId);
        } else {
          ApiTokenStore.HashedToken revokedToken = apiTokenProperty.getTokenStore().revokeToken(value.uuid);
          if (revokedToken == null) {
            LOGGER.log(Level.INFO, "User without selected token id={0}, tokenUuid={1}", new Object[] { value.userId, value.uuid });
          } else {
            apiTokenProperty.deleteApiToken();
            user.save();
            LOGGER.log(Level.INFO, "Revocation success for user id={0}, tokenUuid={1}", new Object[] { value.userId, value.uuid });
          } 
        } 
      } 
    } 
    return HttpResponses.ok();
  }
}
