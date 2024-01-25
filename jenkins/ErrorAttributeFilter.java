package jenkins;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ErrorAttributeFilter implements Filter {
  public static final String USER_ATTRIBUTE = "jenkins.ErrorAttributeFilter.user";
  
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    Authentication authentication = Jenkins.getAuthentication2();
    servletRequest.setAttribute("jenkins.ErrorAttributeFilter.user", authentication);
    filterChain.doFilter(servletRequest, servletResponse);
  }
  
  public void destroy() {}
  
  public void init(FilterConfig filterConfig) throws ServletException {}
}
