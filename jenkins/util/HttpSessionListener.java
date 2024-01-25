package jenkins.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@SuppressFBWarnings(value = {"NM_SAME_SIMPLE_NAME_AS_INTERFACE"}, justification = "Should shadow HttpSessionListener")
public abstract class HttpSessionListener implements ExtensionPoint, HttpSessionListener {
  public static ExtensionList<HttpSessionListener> all() { return ExtensionList.lookup(HttpSessionListener.class); }
  
  public void sessionCreated(HttpSessionEvent httpSessionEvent) {}
  
  public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {}
}
