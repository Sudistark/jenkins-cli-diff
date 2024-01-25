package jenkins.security;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Symbol({"apiToken"})
@Extension
@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
public class ApiCrumbExclusion extends CrumbExclusion {
  public boolean process(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (Boolean.TRUE.equals(request.getAttribute(BasicHeaderApiTokenAuthenticator.class.getName()))) {
      chain.doFilter(request, response);
      return true;
    } 
    return false;
  }
}
