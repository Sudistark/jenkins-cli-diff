package hudson.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import jenkins.util.SystemProperties;

public class CharacterEncodingFilter implements Filter {
  private static final String ENCODING = "UTF-8";
  
  private static final Boolean DISABLE_FILTER = Boolean.valueOf(SystemProperties.getBoolean(CharacterEncodingFilter.class.getName() + ".disableFilter"));
  
  private static final Boolean FORCE_ENCODING = Boolean.valueOf(SystemProperties.getBoolean(CharacterEncodingFilter.class.getName() + ".forceEncoding"));
  
  public void init(FilterConfig filterConfig) throws ServletException { LOGGER.log(Level.FINE, "CharacterEncodingFilter initialized. DISABLE_FILTER: {0} FORCE_ENCODING: {1}", new Object[] { DISABLE_FILTER, FORCE_ENCODING }); }
  
  public void destroy() { LOGGER.fine("CharacterEncodingFilter destroyed."); }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!DISABLE_FILTER.booleanValue() && 
      request instanceof HttpServletRequest) {
      HttpServletRequest req = (HttpServletRequest)request;
      if (shouldSetCharacterEncoding(req))
        req.setCharacterEncoding("UTF-8"); 
    } 
    chain.doFilter(request, response);
  }
  
  private boolean shouldSetCharacterEncoding(HttpServletRequest req) {
    String method = req.getMethod();
    if (!"POST".equalsIgnoreCase(method))
      return false; 
    String contentType = req.getContentType();
    if (contentType != null) {
      boolean isXmlSubmission = (contentType.startsWith("application/xml") || contentType.startsWith("text/xml"));
      if (isXmlSubmission)
        return false; 
    } 
    return (FORCE_ENCODING.booleanValue() || req.getCharacterEncoding() == null);
  }
  
  private static final Logger LOGGER = Logger.getLogger(CharacterEncodingFilter.class.getName());
}
