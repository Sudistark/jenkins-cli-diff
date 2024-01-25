package jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import jenkins.util.HttpSessionListener;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class JenkinsHttpSessionListener implements HttpSessionListener {
  private static final Logger LOGGER = Logger.getLogger(JenkinsHttpSessionListener.class.getName());
  
  public void sessionCreated(HttpSessionEvent httpSessionEvent) {
    for (HttpSessionListener listener : HttpSessionListener.all()) {
      try {
        listener.sessionCreated(httpSessionEvent);
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error calling HttpSessionListener ExtensionPoint sessionCreated().", e);
      } 
    } 
  }
  
  public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
    for (HttpSessionListener listener : HttpSessionListener.all()) {
      try {
        listener.sessionDestroyed(httpSessionEvent);
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error calling HttpSessionListener ExtensionPoint sessionDestroyed().", e);
      } 
    } 
  }
}
