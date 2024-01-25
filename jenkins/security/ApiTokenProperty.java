package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.ReconfigurableDescribable;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.security.ACL;
import hudson.util.HttpResponses;
import hudson.util.Secret;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import jenkins.security.apitoken.ApiTokenStats;
import jenkins.security.apitoken.ApiTokenStore;
import jenkins.security.apitoken.TokenUuidAndPlainValue;
import jenkins.util.SystemProperties;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ApiTokenProperty extends UserProperty {
  private static final Logger LOGGER = Logger.getLogger(ApiTokenProperty.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  private static boolean SHOW_LEGACY_TOKEN_TO_ADMINS = SystemProperties.getBoolean(ApiTokenProperty.class.getName() + ".showTokenToAdmins");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  private static boolean ADMIN_CAN_GENERATE_NEW_TOKENS = SystemProperties.getBoolean(ApiTokenProperty.class.getName() + ".adminCanGenerateNewTokens");
  
  private ApiTokenStore tokenStore;
  
  private ApiTokenStats tokenStats;
  
  @DataBoundConstructor
  public ApiTokenProperty() {}
  
  protected void setUser(User u) {
    super.setUser(u);
    if (this.tokenStore == null)
      this.tokenStore = new ApiTokenStore(); 
    if (this.tokenStats == null)
      this.tokenStats = ApiTokenStats.load(this.user); 
    if (this.apiToken != null)
      this.tokenStore.regenerateTokenFromLegacyIfRequired(this.apiToken); 
  }
  
  ApiTokenProperty(@CheckForNull String seed) {
    if (seed != null)
      this.apiToken = Secret.fromString(seed); 
  }
  
  @NonNull
  public String getApiToken() {
    LOGGER.log(Level.FINE, "Deprecated usage of getApiToken");
    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.log(Level.FINER, "Deprecated usage of getApiToken (trace)", new Exception()); 
    return hasPermissionToSeeToken() ? 
      getApiTokenInsecure() : 
      Messages.ApiTokenProperty_ChangeToken_TokenIsHidden();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasLegacyToken() { return (this.apiToken != null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  @SuppressFBWarnings(value = {"UNSAFE_HASH_EQUALS"}, justification = "Used to prevent use of pre-2013 API tokens, then returning the API token value")
  String getApiTokenInsecure() {
    if (this.apiToken == null)
      return Messages.ApiTokenProperty_NoLegacyToken(); 
    String p = this.apiToken.getPlainText();
    if (p.equals(Util.getDigestOf(Jenkins.get().getSecretKey() + ":" + Jenkins.get().getSecretKey())))
      this.apiToken = Secret.fromString(p = API_KEY_SEED.mac(this.user.getId())); 
    return Util.getDigestOf(p);
  }
  
  public boolean matchesPassword(String token) {
    if (StringUtils.isBlank(token))
      return false; 
    ApiTokenStore.HashedToken matchingToken = this.tokenStore.findMatchingToken(token);
    if (matchingToken == null)
      return false; 
    this.tokenStats.updateUsageForId(matchingToken.getUuid());
    return true;
  }
  
  private boolean hasPermissionToSeeToken() { return canCurrentUserControlObject(SHOW_LEGACY_TOKEN_TO_ADMINS, this.user); }
  
  private static boolean canCurrentUserControlObject(boolean trustAdmins, User propertyOwner) {
    if (trustAdmins && Jenkins.get().hasPermission(Jenkins.ADMINISTER))
      return true; 
    User current = User.current();
    if (current == null)
      return false; 
    if (Jenkins.getAuthentication2().equals(ACL.SYSTEM2))
      return true; 
    return User.idStrategy().equals(propertyOwner.getId(), current.getId());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Collection<TokenInfoAndStats> getTokenList() {
    return (Collection)this.tokenStore.getTokenListSortedByName()
      .stream()
      .map(token -> {
          ApiTokenStats.SingleTokenStats stats = this.tokenStats.findTokenStatsById(token.getUuid());
          return new TokenInfoAndStats(token, stats);
        }).collect(Collectors.toList());
  }
  
  public UserProperty reconfigure(StaplerRequest req, @CheckForNull JSONObject form) throws Descriptor.FormException {
    if (form == null)
      return this; 
    Object tokenStoreData = form.get("tokenStore");
    Map<String, JSONObject> tokenStoreTypedData = convertToTokenMap(tokenStoreData);
    this.tokenStore.reconfigure(tokenStoreTypedData);
    return this;
  }
  
  private Map<String, JSONObject> convertToTokenMap(Object tokenStoreData) {
    if (tokenStoreData == null)
      return Collections.emptyMap(); 
    if (tokenStoreData instanceof JSONObject) {
      JSONObject singleTokenData = (JSONObject)tokenStoreData;
      Map<String, JSONObject> result = new HashMap<String, JSONObject>();
      addJSONTokenIntoMap(result, singleTokenData);
      return result;
    } 
    if (tokenStoreData instanceof JSONArray) {
      JSONArray tokenArray = (JSONArray)tokenStoreData;
      Map<String, JSONObject> result = new HashMap<String, JSONObject>();
      for (int i = 0; i < tokenArray.size(); i++) {
        JSONObject tokenData = tokenArray.getJSONObject(i);
        addJSONTokenIntoMap(result, tokenData);
      } 
      return result;
    } 
    throw HttpResponses.error(400, "Unexpected class received for the token store information");
  }
  
  private void addJSONTokenIntoMap(Map<String, JSONObject> tokenMap, JSONObject tokenData) {
    String uuid = tokenData.getString("tokenUuid");
    tokenMap.put(uuid, tokenData);
  }
  
  @Deprecated
  public void changeApiToken() {
    this.user.checkPermission(Jenkins.ADMINISTER);
    LOGGER.log(Level.FINE, "Deprecated usage of changeApiToken");
    ApiTokenStore.HashedToken existingLegacyToken = this.tokenStore.getLegacyToken();
    _changeApiToken();
    this.tokenStore.regenerateTokenFromLegacy(this.apiToken);
    if (existingLegacyToken != null)
      this.tokenStats.removeId(existingLegacyToken.getUuid()); 
    this.user.save();
  }
  
  @Deprecated
  private void _changeApiToken() {
    byte[] random = new byte[16];
    RANDOM.nextBytes(random);
    this.apiToken = Secret.fromString(Util.toHexString(random));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void deleteApiToken() { this.apiToken = null; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public ApiTokenStore getTokenStore() { return this.tokenStore; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public ApiTokenStats getTokenStats() { return this.tokenStats; }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  @NonNull
  public String addFixedNewToken(@NonNull String name, @NonNull String tokenPlainValue) throws IOException {
    String tokenUuid = this.tokenStore.addFixedNewToken(name, tokenPlainValue);
    this.user.save();
    return tokenUuid;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  @NonNull
  public TokenUuidAndPlainValue generateNewToken(@NonNull String name) throws IOException {
    TokenUuidAndPlainValue tokenUuidAndPlainValue = this.tokenStore.generateNewToken(name);
    this.user.save();
    return tokenUuidAndPlainValue;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void revokeAllTokens() {
    this.tokenStats.removeAll();
    this.tokenStore.revokeAllTokens();
    this.user.save();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void revokeAllTokensExceptOne(@NonNull String tokenUuid) {
    this.tokenStats.removeAllExcept(tokenUuid);
    this.tokenStore.revokeAllTokensExcept(tokenUuid);
    this.user.save();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void revokeToken(@NonNull String tokenUuid) {
    ApiTokenStore.HashedToken revoked = this.tokenStore.revokeToken(tokenUuid);
    if (revoked != null) {
      if (revoked.isLegacy())
        this.apiToken = null; 
      this.tokenStats.removeId(revoked.getUuid());
      this.user.save();
    } 
  }
  
  @Deprecated
  private static final SecureRandom RANDOM = new SecureRandom();
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final HMACConfidentialKey API_KEY_SEED = new HMACConfidentialKey(ApiTokenProperty.class, "seed", 16);
}
