package hudson.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.commons.jelly.JellyTagException;

public class UnwrapSecurityExceptionFilter implements Filter {
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (ServletException e) {
      Throwable t = e.getRootCause();
      if (t != null && !(t instanceof JellyTagException))
        if (t instanceof ServletException) {
          t = ((ServletException)t).getRootCause();
        } else {
          t = t.getCause();
        }  
      if (t instanceof JellyTagException) {
        JellyTagException jte = (JellyTagException)t;
        Throwable cause = jte.getCause();
        if (cause instanceof org.springframework.security.access.AccessDeniedException || cause instanceof org.springframework.security.core.AuthenticationException)
          throw new ServletException(cause); 
      } 
      throw e;
    } 
  }
  
  public void destroy() {}
}
