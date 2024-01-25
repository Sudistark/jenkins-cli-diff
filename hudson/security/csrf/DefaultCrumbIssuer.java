package hudson.security.csrf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.security.core.Authentication;

public class DefaultCrumbIssuer extends CrumbIssuer {
  private MessageDigest md;
  
  private boolean excludeClientIPFromCrumb;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean EXCLUDE_SESSION_ID = SystemProperties.getBoolean(DefaultCrumbIssuer.class.getName() + ".EXCLUDE_SESSION_ID");
  
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";
  
  @DataBoundConstructor
  public DefaultCrumbIssuer(boolean excludeClientIPFromCrumb) {
    this.excludeClientIPFromCrumb = excludeClientIPFromCrumb;
    initializeMessageDigest();
  }
  
  public boolean isExcludeClientIPFromCrumb() { return this.excludeClientIPFromCrumb; }
  
  private Object readResolve() {
    initializeMessageDigest();
    return this;
  }
  
  private void initializeMessageDigest() {
    try {
      this.md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      this.md = null;
      LOGGER.log(Level.SEVERE, e, () -> "Cannot find SHA-256 MessageDigest implementation.");
    } 
  }
  
  protected String issueCrumb(ServletRequest request, String salt) {
    if (request instanceof HttpServletRequest && 
      this.md != null) {
      HttpServletRequest req = (HttpServletRequest)request;
      StringBuilder buffer = new StringBuilder();
      Authentication a = Jenkins.getAuthentication2();
      buffer.append(a.getName());
      buffer.append(';');
      if (!isExcludeClientIPFromCrumb())
        buffer.append(getClientIP(req)); 
      if (!EXCLUDE_SESSION_ID) {
        buffer.append(';');
        buffer.append(req.getSession().getId());
      } 
      this.md.update(buffer.toString().getBytes(StandardCharsets.UTF_8));
      return Util.toHexString(this.md.digest(salt.getBytes(StandardCharsets.US_ASCII)));
    } 
    return null;
  }
  
  public boolean validateCrumb(ServletRequest request, String salt, String crumb) {
    if (request instanceof HttpServletRequest) {
      String newCrumb = issueCrumb(request, salt);
      if (newCrumb != null && crumb != null)
        return MessageDigest.isEqual(newCrumb.getBytes(StandardCharsets.US_ASCII), crumb
            .getBytes(StandardCharsets.US_ASCII)); 
    } 
    return false;
  }
  
  private String getClientIP(HttpServletRequest req) {
    String defaultAddress = req.getRemoteAddr();
    String forwarded = req.getHeader("X-Forwarded-For");
    if (forwarded != null) {
      String[] hopList = forwarded.split(",");
      if (hopList.length >= 1)
        return hopList[0]; 
    } 
    return defaultAddress;
  }
  
  private static final Logger LOGGER = Logger.getLogger(DefaultCrumbIssuer.class.getName());
}
