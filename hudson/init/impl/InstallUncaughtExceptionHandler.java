package hudson.init.impl;

import hudson.init.Initializer;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.compression.CompressionFilter;

public class InstallUncaughtExceptionHandler {
  private static final Logger LOGGER = Logger.getLogger(InstallUncaughtExceptionHandler.class.getName());
  
  @Initializer
  public static void init(Jenkins j) throws IOException {
    CompressionFilter.setUncaughtExceptionHandler(j.servletContext, (e, context, req, rsp) -> handleException(j, e, req, rsp, 500));
    try {
      Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());
      LOGGER.log(Level.FINE, "Successfully installed a global UncaughtExceptionHandler.");
    } catch (SecurityException ex) {
      LOGGER.log(Level.SEVERE, "Failed to set the default UncaughtExceptionHandler. If any threads die due to unhandled coding errors then there will be no logging of this information. The lack of this diagnostic information will make it harder to track down issues which will reduce the supportability of Jenkins. It is highly recommended that you consult the documentation that comes with your servlet container on how to allow the `setDefaultUncaughtExceptionHandler` permission and enable it.", ex);
    } 
  }
  
  private static void handleException(Jenkins j, Throwable e, HttpServletRequest req, HttpServletResponse rsp, int code) throws IOException, ServletException {
    if (rsp.isCommitted()) {
      LOGGER.log(isEOFException(e) ? Level.FINE : Level.WARNING, null, e);
      return;
    } 
    String id = UUID.randomUUID().toString();
    LOGGER.log(isEOFException(e) ? Level.FINE : Level.WARNING, "Caught unhandled exception with ID " + id, e);
    req.setAttribute("jenkins.exception.id", id);
    req.setAttribute("javax.servlet.error.exception", e);
    rsp.setStatus(code);
    try {
      WebApp.get(j.servletContext).getSomeStapler().invoke(req, rsp, j, "/oops");
    } catch (ServletException|IOException x) {
      if (!Stapler.isSocketException(x))
        throw x; 
    } 
  }
  
  private static boolean isEOFException(Throwable e) {
    if (e == null)
      return false; 
    if (e instanceof java.io.EOFException)
      return true; 
    return isEOFException(e.getCause());
  }
}
