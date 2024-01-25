package jenkins.websocket;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public class WebSockets {
  private static final Logger LOGGER = Logger.getLogger(WebSockets.class.getName());
  
  private static final Provider provider = findProvider();
  
  private static Provider findProvider() {
    it = ServiceLoader.load(Provider.class).iterator();
    while (it.hasNext()) {
      try {
        return (Provider)it.next();
      } catch (ServiceConfigurationError x) {
        LOGGER.log(Level.FINE, null, x);
      } 
    } 
    return null;
  }
  
  public static HttpResponse upgrade(WebSocketSession session) {
    if (provider == null)
      throw HttpResponses.notFound(); 
    return (req, rsp, node) -> {
        try {
          session.handler = provider.handle(req, rsp, new Object(session));
        } catch (Exception x) {
          LOGGER.log(Level.WARNING, null, x);
          throw HttpResponses.error(x);
        } 
      };
  }
  
  public static boolean isSupported() { return (provider != null); }
}
