package hudson.util;

import hudson.remoting.Which;
import java.io.IOException;
import java.net.URL;

public class IncompatibleServletVersionDetected extends BootFailure {
  private final Class servletClass;
  
  public IncompatibleServletVersionDetected(Class servletClass) { this.servletClass = servletClass; }
  
  public URL getWhereServletIsLoaded() throws IOException { return Which.classFileUrl(this.servletClass); }
}
