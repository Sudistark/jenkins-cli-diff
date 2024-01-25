package jenkins.websocket;

import hudson.Extension;
import hudson.model.InvisibleAction;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class WebSocketEcho extends InvisibleAction implements RootAction {
  public String getUrlName() { return "wsecho"; }
  
  public HttpResponse doIndex() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    return WebSockets.upgrade(new Object(this));
  }
}
