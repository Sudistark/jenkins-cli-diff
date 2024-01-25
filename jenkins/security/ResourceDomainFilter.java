package jenkins.security;

import hudson.Extension;
import hudson.Functions;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.util.HttpServletFilter;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ResourceDomainFilter implements HttpServletFilter {
  private static final Logger LOGGER = Logger.getLogger(ResourceDomainFilter.class.getName());
  
  private static final Set<String> ALLOWED_PATHS = new HashSet(Arrays.asList(new String[] { "/static-files", "/favicon.ico", "/favicon.svg", "/apple-touch-icon.png", "/mask-icon.svg", "/robots.txt", "/images/rage.svg" }));
  
  public static final String ERROR_RESPONSE = "Jenkins serves only static files on this domain.";
  
  public boolean handle(HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
    if (ResourceDomainConfiguration.isResourceRequest(req)) {
      String path = req.getPathInfo();
      if (!path.startsWith("/static-files/") && !ALLOWED_PATHS.contains(path) && !isAllowedPathWithResourcePrefix(path)) {
        LOGGER.fine(() -> "Rejecting request to " + req.getRequestURL() + " from " + req.getRemoteAddr() + " on resource domain");
        rsp.sendError(404, "Jenkins serves only static files on this domain.");
        return true;
      } 
      LOGGER.finer(() -> "Accepting request to " + req.getRequestURL() + " from " + req.getRemoteAddr() + " on resource domain");
    } 
    return false;
  }
  
  private static boolean isAllowedPathWithResourcePrefix(String path) { return (path.startsWith(Functions.getResourcePath()) && ALLOWED_PATHS.contains(path.substring(Functions.getResourcePath().length()))); }
}
