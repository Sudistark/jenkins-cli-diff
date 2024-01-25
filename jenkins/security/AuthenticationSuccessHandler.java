package jenkins.security;

import hudson.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String originalTargetUrl = super.determineTargetUrl(request, response, authentication);
    String contextPath = request.getContextPath();
    if (originalTargetUrl.startsWith(contextPath))
      originalTargetUrl = originalTargetUrl.substring(contextPath.length()); 
    if (Util.isSafeToRedirectTo(originalTargetUrl))
      return originalTargetUrl; 
    return getDefaultTargetUrl();
  }
}
