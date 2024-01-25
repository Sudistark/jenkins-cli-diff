package hudson.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ChainedServletFilter implements Filter {
  public ChainedServletFilter() { this.filters = new Filter[0]; }
  
  public ChainedServletFilter(Filter... filters) { this(Arrays.asList(filters)); }
  
  public ChainedServletFilter(Collection<? extends Filter> filters) { setFilters(filters); }
  
  public void setFilters(Collection<? extends Filter> filters) { this.filters = (Filter[])filters.toArray(new Filter[0]); }
  
  public void init(FilterConfig filterConfig) throws ServletException {
    if (LOGGER.isLoggable(Level.FINEST))
      for (Filter f : this.filters)
        LOGGER.finest("ChainedServletFilter contains: " + f);  
    for (Filter f : this.filters)
      f.init(filterConfig); 
  }
  
  private static final Pattern UNINTERESTING_URIS = Pattern.compile("/(images|jsbundles|css|scripts|adjuncts)/|/favicon[.](ico|svg)|/ajax");
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    String uri = (request instanceof HttpServletRequest) ? ((HttpServletRequest)request).getRequestURI() : "?";
    Level level = UNINTERESTING_URIS.matcher(uri).find() ? Level.FINER : Level.FINE;
    LOGGER.log(level, () -> "starting filter on " + uri);
    (new Object(this, level, uri, chain, response))
























      
      .doFilter(request, response);
  }
  
  public void destroy() {
    for (Filter f : this.filters)
      f.destroy(); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(ChainedServletFilter.class.getName());
}
