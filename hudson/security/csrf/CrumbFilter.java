package hudson.security.csrf;

import hudson.util.MultipartFormDataParser;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;

public class CrumbFilter implements Filter {
  public CrumbIssuer getCrumbIssuer() {
    Jenkins h = Jenkins.getInstanceOrNull();
    if (h == null)
      return null; 
    return h.getCrumbIssuer();
  }
  
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    CrumbIssuer crumbIssuer = getCrumbIssuer();
    if (crumbIssuer == null || !(request instanceof HttpServletRequest)) {
      chain.doFilter(request, response);
      return;
    } 
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    HttpServletResponse httpResponse = (HttpServletResponse)response;
    if ("POST".equals(httpRequest.getMethod())) {
      HttpServletRequest wrappedRequest = UNPROCESSED_PATHINFO ? httpRequest : new Security1774ServletRequest(httpRequest);
      for (CrumbExclusion e : CrumbExclusion.all()) {
        if (e.process(wrappedRequest, httpResponse, chain))
          return; 
      } 
      String crumbFieldName = crumbIssuer.getDescriptor().getCrumbRequestField();
      String crumbSalt = crumbIssuer.getDescriptor().getCrumbSalt();
      boolean valid = false;
      String crumb = extractCrumbFromRequest(httpRequest, crumbFieldName);
      if (crumb == null)
        extractCrumbFromRequest(httpRequest, ".crumb"); 
      Level level = (Jenkins.getAuthentication2() instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) ? Level.FINE : Level.WARNING;
      if (crumb != null)
        if (crumbIssuer.validateCrumb(httpRequest, crumbSalt, crumb)) {
          valid = true;
        } else {
          LOGGER.log(level, "Found invalid crumb {0}. If you are calling this URL with a script, please use the API Token instead. More information: https://www.jenkins.io/redirect/crumb-cannot-be-used-for-script", crumb);
        }  
      if (valid) {
        chain.doFilter(request, response);
      } else {
        LOGGER.log(level, "No valid crumb was included in request for {0} by {1}. Returning {2}.", new Object[] { httpRequest.getRequestURI(), Jenkins.getAuthentication2().getName(), Integer.valueOf(403) });
        httpResponse.sendError(403, "No valid crumb was included in the request");
      } 
    } else {
      chain.doFilter(request, response);
    } 
  }
  
  private String extractCrumbFromRequest(HttpServletRequest httpRequest, String crumbFieldName) {
    String crumb = httpRequest.getHeader(crumbFieldName);
    if (crumb == null) {
      Enumeration<?> paramNames = httpRequest.getParameterNames();
      while (paramNames.hasMoreElements()) {
        String paramName = (String)paramNames.nextElement();
        if (crumbFieldName.equals(paramName)) {
          crumb = httpRequest.getParameter(paramName);
          break;
        } 
      } 
    } 
    return crumb;
  }
  
  protected static boolean isMultipart(HttpServletRequest request) {
    if (request == null)
      return false; 
    return MultipartFormDataParser.isMultiPartForm(request.getContentType());
  }
  
  public void destroy() {}
  
  static boolean UNPROCESSED_PATHINFO = SystemProperties.getBoolean(CrumbFilter.class.getName() + ".UNPROCESSED_PATHINFO");
  
  private static final Logger LOGGER = Logger.getLogger(CrumbFilter.class.getName());
}
