package hudson.security;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.User;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import jenkins.security.HMACConfidentialKey;
import jenkins.security.ImpersonatingUserDetailsService2;
import jenkins.security.seed.UserSeedProperty;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class TokenBasedRememberMeServices2 extends AbstractRememberMeServices {
  private static final Logger LOGGER = Logger.getLogger(TokenBasedRememberMeServices2.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_TOO_FAR_EXPIRATION_DATE_CHECK = SystemProperties.getBoolean(TokenBasedRememberMeServices2.class.getName() + ".skipTooFarExpirationDateCheck");
  
  public TokenBasedRememberMeServices2(UserDetailsService userDetailsService) { super(Jenkins.get().getSecretKey(), new ImpersonatingUserDetailsService2(userDetailsService)); }
  
  protected String makeTokenSignature(long tokenExpiryTime, String username) {
    String userSeed;
    if (UserSeedProperty.DISABLE_USER_SEED) {
      userSeed = "no-seed";
    } else {
      User user = User.getById(username, true);
      UserSeedProperty userSeedProperty = (UserSeedProperty)user.getProperty(UserSeedProperty.class);
      if (userSeedProperty == null)
        return "no-prop"; 
      userSeed = userSeedProperty.getSeed();
    } 
    String token = String.join(":", new CharSequence[] { username, Long.toString(tokenExpiryTime), userSeed, getKey() });
    return MAC.mac(token);
  }
  
  public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
    if (!rememberMeRequested(request, getParameter())) {
      if (this.logger.isDebugEnabled())
        this.logger.debug("Did not send remember-me cookie (principal did not set parameter '" + 
            getParameter() + "')"); 
      return;
    } 
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null && j.isDisableRememberMe()) {
      if (this.logger.isDebugEnabled())
        this.logger.debug("Did not send remember-me cookie because 'Remember Me' is disabled in security configuration (principal did set parameter '" + 
            getParameter() + "')"); 
      return;
    } 
    long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(getTokenValiditySeconds());
    String username = successfulAuthentication.getName();
    String signatureValue = makeTokenSignature(expiryTime, username);
    int tokenLifetime = calculateLoginLifetime(request, successfulAuthentication);
    setCookie(new String[] { username, Long.toString(expiryTime), signatureValue }, tokenLifetime, request, response);
    if (this.logger.isDebugEnabled())
      this.logger.debug("Added remember-me cookie for user '" + username + "', expiry: '" + new Date(expiryTime) + "'"); 
  }
  
  protected int calculateLoginLifetime(HttpServletRequest request, Authentication authentication) { return getTokenValiditySeconds(); }
  
  protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null)
      throw new InvalidCookieException("Jenkins is not yet running"); 
    if (j.isDisableRememberMe()) {
      cancelCookie(request, response);
      throw new InvalidCookieException("rememberMe is disabled");
    } 
    if (cookieTokens.length != 3)
      throw new InvalidCookieException("Cookie token did not contain 3 tokens, but contained '" + 
          Arrays.asList(cookieTokens) + "'"); 
    long tokenExpiryTime = getTokenExpiryTime(cookieTokens);
    if (isTokenExpired(tokenExpiryTime))
      throw new InvalidCookieException("Cookie token[1] has expired (expired on '" + new Date(tokenExpiryTime) + "'; current time is '" + new Date() + "')"); 
    UserDetails userDetails = getUserDetailsService().loadUserByUsername(cookieTokens[0]);
    Objects.requireNonNull(userDetails, "UserDetailsService " + getUserDetailsService() + " returned null for username " + cookieTokens[0] + ". This is an interface contract violation");
    String expectedTokenSignature = makeTokenSignature(tokenExpiryTime, userDetails.getUsername());
    if (!equals(expectedTokenSignature, cookieTokens[2]))
      throw new InvalidCookieException("Cookie token[2] contained signature '" + cookieTokens[2] + "' but expected '" + expectedTokenSignature + "'"); 
    return userDetails;
  }
  
  private long getTokenExpiryTime(String[] cookieTokens) {
    try {
      return Long.parseLong(cookieTokens[1]);
    } catch (NumberFormatException nfe) {
      throw new InvalidCookieException("Cookie token[1] did not contain a valid number (contained '" + cookieTokens[1] + "')");
    } 
  }
  
  @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"}, justification = "TODO needs triage")
  protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails userDetails) {
    Authentication auth = super.createSuccessfulAuthentication(request, userDetails);
    if (!UserSeedProperty.DISABLE_USER_SEED) {
      User user = User.get2(auth);
      UserSeedProperty userSeed = (UserSeedProperty)user.getProperty(UserSeedProperty.class);
      String sessionSeed = userSeed.getSeed();
      request.getSession().setAttribute("_JENKINS_SESSION_SEED", sessionSeed);
    } 
    return auth;
  }
  
  protected boolean isTokenExpired(long tokenExpiryTimeMs) {
    long nowMs = System.currentTimeMillis();
    long maxExpirationMs = TimeUnit.SECONDS.toMillis(getTokenValiditySeconds()) + nowMs;
    if (!SKIP_TOO_FAR_EXPIRATION_DATE_CHECK && tokenExpiryTimeMs > maxExpirationMs) {
      long diffMs = tokenExpiryTimeMs - maxExpirationMs;
      LOGGER.log(Level.WARNING, "Attempt to use a cookie with an expiration duration larger than the one configured (delta of: {0} ms)", Long.valueOf(diffMs));
      return true;
    } 
    if (tokenExpiryTimeMs < nowMs)
      return true; 
    return false;
  }
  
  @VisibleForTesting
  protected int getTokenValiditySeconds() { return super.getTokenValiditySeconds(); }
  
  @VisibleForTesting
  protected String getCookieName() { return super.getCookieName(); }
  
  private static boolean equals(String expected, String actual) {
    byte[] expectedBytes = bytesUtf8(expected);
    byte[] actualBytes = bytesUtf8(actual);
    return MessageDigest.isEqual(expectedBytes, actualBytes);
  }
  
  private static byte[] bytesUtf8(String s) { return (s != null) ? Utf8.encode(s) : null; }
  
  private static final HMACConfidentialKey MAC = new HMACConfidentialKey(org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.class, "mac");
}
