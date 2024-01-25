package jenkins.security.apitoken;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import hudson.util.Secret;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ApiTokenStore {
  private static final Logger LOGGER = Logger.getLogger(ApiTokenStore.class.getName());
  
  private static final SecureRandom RANDOM = new SecureRandom();
  
  private static final Comparator<HashedToken> SORT_BY_LOWERCASED_NAME = Comparator.comparing(hashedToken -> hashedToken.getName().toLowerCase(Locale.ENGLISH));
  
  private static final int TOKEN_LENGTH_V2 = 34;
  
  private static final String LEGACY_VERSION = "10";
  
  private static final String HASH_VERSION = "11";
  
  private static final String HASH_ALGORITHM = "SHA-256";
  
  private List<HashedToken> tokenList;
  
  private static final int VERSION_LENGTH = 2;
  
  private static final int HEX_CHAR_LENGTH = 32;
  
  public ApiTokenStore() { init(); }
  
  private Object readResolve() {
    init();
    return this;
  }
  
  private void init() {
    if (this.tokenList == null)
      this.tokenList = new ArrayList(); 
  }
  
  @NonNull
  public Collection<HashedToken> getTokenListSortedByName() {
    return (Collection)this.tokenList.stream()
      .sorted(SORT_BY_LOWERCASED_NAME)
      .collect(Collectors.toList());
  }
  
  private void addToken(HashedToken token) { this.tokenList.add(token); }
  
  public void reconfigure(@NonNull Map<String, JSONObject> tokenStoreDataMap) {
    this.tokenList.forEach(hashedToken -> {
          JSONObject receivedTokenData = (JSONObject)tokenStoreDataMap.get(hashedToken.uuid);
          if (receivedTokenData == null) {
            LOGGER.log(Level.INFO, "No token received for {0}", hashedToken.uuid);
            return;
          } 
          String name = receivedTokenData.getString("tokenName");
          if (StringUtils.isBlank(name)) {
            LOGGER.log(Level.INFO, "Empty name received for {0}, we do not care about it", hashedToken.uuid);
            return;
          } 
          hashedToken.setName(name);
        });
  }
  
  public void regenerateTokenFromLegacy(@NonNull Secret newLegacyApiToken) { deleteAllLegacyAndGenerateNewOne(newLegacyApiToken, false); }
  
  public void regenerateTokenFromLegacyIfRequired(@NonNull Secret newLegacyApiToken) {
    if (this.tokenList.stream().noneMatch(HashedToken::isLegacy))
      deleteAllLegacyAndGenerateNewOne(newLegacyApiToken, true); 
  }
  
  private void deleteAllLegacyAndGenerateNewOne(@NonNull Secret newLegacyApiToken, boolean migrationFromExistingLegacy) {
    deleteAllLegacyTokens();
    addLegacyToken(newLegacyApiToken, migrationFromExistingLegacy);
  }
  
  private void deleteAllLegacyTokens() { this.tokenList.removeIf(HashedToken::isLegacy); }
  
  private void addLegacyToken(@NonNull Secret legacyToken, boolean migrationFromExistingLegacy) {
    String tokenUserUseNormally = Util.getDigestOf(legacyToken.getPlainText());
    String secretValueHashed = plainSecretToHashInHex(tokenUserUseNormally);
    HashValue hashValue = new HashValue("10", secretValueHashed);
    HashedToken token = HashedToken.buildNewFromLegacy(hashValue, migrationFromExistingLegacy);
    addToken(token);
  }
  
  @Nullable
  public HashedToken getLegacyToken() {
    return (HashedToken)this.tokenList.stream()
      .filter(HashedToken::isLegacy)
      .findFirst()
      .orElse(null);
  }
  
  @NonNull
  public TokenUuidAndPlainValue generateNewToken(@NonNull String name) {
    byte[] random = new byte[16];
    RANDOM.nextBytes(random);
    String secretValue = Util.toHexString(random);
    String tokenTheUserWillUse = "11" + secretValue;
    assert tokenTheUserWillUse.length() == 34;
    HashedToken token = prepareAndStoreToken(name, secretValue);
    return new TokenUuidAndPlainValue(token.uuid, tokenTheUserWillUse);
  }
  
  private static final Pattern CHECK_32_HEX_CHAR = Pattern.compile("[a-f0-9]{32}");
  
  @SuppressFBWarnings(value = {"UNSAFE_HASH_EQUALS"}, justification = "Comparison only validates version of the specified token")
  @NonNull
  public String addFixedNewToken(@NonNull String name, @NonNull String tokenPlainValue) {
    if (tokenPlainValue.length() != 34) {
      LOGGER.log(Level.INFO, "addFixedNewToken, length received: {0}" + tokenPlainValue.length());
      throw new IllegalArgumentException("The token must consist of 2 characters for the version and 32 hex-characters for the secret");
    } 
    String hashVersion = tokenPlainValue.substring(0, 2);
    if (!"11".equals(hashVersion))
      throw new IllegalArgumentException("The given version is not recognized: " + hashVersion); 
    String tokenPlainHexValue = tokenPlainValue.substring(2);
    tokenPlainHexValue = tokenPlainHexValue.toLowerCase();
    if (!CHECK_32_HEX_CHAR.matcher(tokenPlainHexValue).matches())
      throw new IllegalArgumentException("The secret part of the token must consist of 32 hex-characters"); 
    HashedToken token = prepareAndStoreToken(name, tokenPlainHexValue);
    return token.uuid;
  }
  
  @NonNull
  private HashedToken prepareAndStoreToken(@NonNull String name, @NonNull String tokenPlainValue) {
    String secretValueHashed = plainSecretToHashInHex(tokenPlainValue);
    HashValue hashValue = new HashValue("11", secretValueHashed);
    HashedToken token = HashedToken.buildNew(name, hashValue);
    addToken(token);
    return token;
  }
  
  @NonNull
  private String plainSecretToHashInHex(@NonNull String secretValueInPlainText) {
    byte[] hashBytes = plainSecretToHashBytes(secretValueInPlainText);
    return Util.toHexString(hashBytes);
  }
  
  @NonNull
  private byte[] plainSecretToHashBytes(@NonNull String secretValueInPlainText) { return hashedBytes(secretValueInPlainText.getBytes(StandardCharsets.US_ASCII)); }
  
  @NonNull
  private byte[] hashedBytes(byte[] tokenBytes) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError("There is no SHA-256 available in this system", e);
    } 
    return digest.digest(tokenBytes);
  }
  
  @CheckForNull
  public HashedToken findMatchingToken(@NonNull String token) {
    String plainToken;
    if (isLegacyToken(token)) {
      plainToken = token;
    } else {
      plainToken = getHashOfToken(token);
    } 
    return searchMatch(plainToken);
  }
  
  private boolean isLegacyToken(@NonNull String token) { return (token.length() != 34); }
  
  @NonNull
  private String getHashOfToken(@NonNull String token) { return token.substring(2); }
  
  @CheckForNull
  private HashedToken searchMatch(@NonNull String plainSecret) {
    byte[] hashedBytes = plainSecretToHashBytes(plainSecret);
    for (HashedToken token : this.tokenList) {
      if (token.match(hashedBytes))
        return token; 
    } 
    return null;
  }
  
  @SuppressFBWarnings(value = {"UNSAFE_HASH_EQUALS"}, justification = "Only used during revocation.")
  @CheckForNull
  public HashedToken revokeToken(@NonNull String tokenUuid) {
    for (Iterator<HashedToken> iterator = this.tokenList.iterator(); iterator.hasNext(); ) {
      HashedToken token = (HashedToken)iterator.next();
      if (token.uuid.equals(tokenUuid)) {
        iterator.remove();
        return token;
      } 
    } 
    return null;
  }
  
  public void revokeAllTokens() { this.tokenList.clear(); }
  
  public void revokeAllTokensExcept(@NonNull String tokenUuid) { this.tokenList.removeIf(token -> !token.uuid.equals(tokenUuid)); }
  
  public boolean renameToken(@NonNull String tokenUuid, @NonNull String newName) {
    for (HashedToken token : this.tokenList) {
      if (token.uuid.equals(tokenUuid)) {
        token.rename(newName);
        return true;
      } 
    } 
    LOGGER.log(Level.FINER, "The target token for rename does not exist, for uuid = {0}, with desired name = {1}", new Object[] { tokenUuid, newName });
    return false;
  }
}
