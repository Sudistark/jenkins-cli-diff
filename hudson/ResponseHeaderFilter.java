package hudson;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ResponseHeaderFilter implements Filter {
  private FilterConfig config;
  
  public void init(FilterConfig filterConfig) throws ServletException { this.config = filterConfig; }
  
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    HttpServletResponse httpResp = (HttpServletResponse)resp;
    Enumeration e = this.config.getInitParameterNames();
    while (e.hasMoreElements()) {
      String headerName = (String)e.nextElement();
      String headerValue = this.config.getInitParameter(headerName);
      httpResp.setHeader(headerName, headerValue);
    } 
    chain.doFilter(req, resp);
  }
  
  public void destroy() {}
}
