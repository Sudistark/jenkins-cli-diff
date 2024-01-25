package jenkins.util;

import hudson.ExtensionPoint;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;

public interface HttpServletFilter extends ExtensionPoint {
  boolean handle(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse) throws IOException, ServletException;
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @Initializer
  static void register() throws ServletException { PluginServletFilter.addFilter(new Object()); }
}
