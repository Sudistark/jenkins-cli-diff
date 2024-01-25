package jenkins.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class SuspiciousRequestFilter implements Filter {
  public static final String ALLOW_SEMICOLONS_IN_PATH = SuspiciousRequestFilter.class.getName() + ".allowSemicolonsInPath";
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean allowSemicolonsInPath = SystemProperties.getBoolean(ALLOW_SEMICOLONS_IN_PATH, false);
  
  private static final Logger LOGGER = Logger.getLogger(SuspiciousRequestFilter.class.getName());
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    HttpServletResponse httpResponse = (HttpServletResponse)response;
    if (!allowSemicolonsInPath && httpRequest.getRequestURI().contains(";")) {
      LOGGER.warning(() -> "Denying HTTP " + httpRequest.getMethod() + " to " + httpRequest.getRequestURI() + " as it has an illegal semicolon in the path. This behavior can be overridden by setting the system property " + ALLOW_SEMICOLONS_IN_PATH + " to true. For more information, see https://www.jenkins.io/redirect/semicolons-in-urls");
      httpResponse.sendError(400, "Semicolons are not allowed in the request URI");
    } else {
      chain.doFilter(request, response);
    } 
  }
  
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  public void destroy() {}
}
