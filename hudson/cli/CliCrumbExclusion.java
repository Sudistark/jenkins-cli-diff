package hudson.cli;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
public class CliCrumbExclusion extends CrumbExclusion {
  public boolean process(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    String pathInfo = request.getPathInfo();
    if ("/cli".equals(pathInfo)) {
      chain.doFilter(request, response);
      return true;
    } 
    return false;
  }
}
