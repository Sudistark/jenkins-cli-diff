package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.UnprotectedRootAction;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.security.core.Authentication;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ResourceDomainRootAction implements UnprotectedRootAction {
  private static final String RESOURCE_DOMAIN_ROOT_ACTION_ERROR = "jenkins.security.ResourceDomainRootAction.error";
  
  private static final Logger LOGGER = Logger.getLogger(ResourceDomainRootAction.class.getName());
  
  public static final String URL = "static-files";
  
  @CheckForNull
  public String getIconFileName() { return null; }
  
  @CheckForNull
  public String getDisplayName() { return null; }
  
  @CheckForNull
  public String getUrlName() { return "static-files"; }
  
  public static ResourceDomainRootAction get() { return (ResourceDomainRootAction)ExtensionList.lookupSingleton(ResourceDomainRootAction.class); }
  
  public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (ResourceDomainConfiguration.isResourceRequest(req)) {
      rsp.sendError(404, "Jenkins serves only static files on this domain.");
    } else {
      req.setAttribute("jenkins.security.ResourceDomainRootAction.error", Boolean.valueOf(true));
      rsp.sendError(404, "Cannot handle requests to this URL unless on Jenkins resource URL.");
    } 
  }
  
  public Object getDynamic(String id, StaplerRequest req, StaplerResponse rsp) throws Exception {
    if (!ResourceDomainConfiguration.isResourceRequest(req)) {
      req.setAttribute("jenkins.security.ResourceDomainRootAction.error", Boolean.valueOf(true));
      rsp.sendError(404, "Cannot handle requests to this URL unless on Jenkins resource URL.");
      return null;
    } 
    Token token = Token.decode(id);
    if (token == null) {
      rsp.sendError(404, "Jenkins serves only static files on this domain.");
      return null;
    } 
    String authenticationName = token.username;
    String browserUrl = token.path;
    if (token.timestamp.plus(VALID_FOR_MINUTES, ChronoUnit.MINUTES).isAfter(Instant.now()) && token.timestamp.isBefore(Instant.now()))
      return new InternalResourceRequest(browserUrl, authenticationName); 
    return new Redirection(browserUrl);
  }
  
  public String getRedirectUrl(@NonNull Token token, @NonNull String restOfPath) {
    String resourceRootUrl = getResourceRootUrl();
    if (!restOfPath.startsWith("/"))
      restOfPath = "/" + restOfPath; 
    return resourceRootUrl + resourceRootUrl + "/" + getUrlName() + token.encode();
  }
  
  private static String getResourceRootUrl() { return ResourceDomainConfiguration.get().getUrl(); }
  
  @CheckForNull
  public Token getToken(@NonNull DirectoryBrowserSupport dbs, @NonNull StaplerRequest req) {
    String dbsFile = req.getOriginalRestOfPath();
    String completeUrl = ((Ancestor)req.getAncestors().get(0)).getRestOfUrl();
    String dbsUrl = completeUrl.substring(0, completeUrl.length() - dbsFile.length());
    LOGGER.fine(() -> "Determined DBS URL: " + dbsUrl + " from restOfUrl: " + completeUrl + " and restOfPath: " + dbsFile);
    Authentication authentication = Jenkins.getAuthentication2();
    String authenticationName = authentication.equals(Jenkins.ANONYMOUS2) ? "" : authentication.getName();
    try {
      return new Token(dbsUrl, authenticationName, Instant.now());
    } catch (RuntimeException ex) {
      LOGGER.log(Level.WARNING, "Failed to encode token for URL: " + dbsUrl + " user: " + authenticationName, ex);
      return null;
    } 
  }
  
  private static HMACConfidentialKey KEY = new HMACConfidentialKey(ResourceDomainRootAction.class, "key");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static int VALID_FOR_MINUTES = SystemProperties.getInteger(ResourceDomainRootAction.class.getName() + ".validForMinutes", Integer.valueOf(30)).intValue();
}
