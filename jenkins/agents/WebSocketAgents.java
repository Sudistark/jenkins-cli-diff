package jenkins.agents;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.InvisibleAction;
import hudson.model.UnprotectedRootAction;
import hudson.remoting.Capability;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.slaves.JnlpAgentReceiver;
import jenkins.slaves.RemotingVersionInfo;
import jenkins.websocket.WebSockets;
import org.jenkinsci.remoting.engine.JnlpConnectionState;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class WebSocketAgents extends InvisibleAction implements UnprotectedRootAction {
  private static final Logger LOGGER = Logger.getLogger(WebSocketAgents.class.getName());
  
  public String getUrlName() { return WebSockets.isSupported() ? "wsagents" : null; }
  
  public HttpResponse doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
    String cookie, agent = req.getHeader("Node-Name");
    String secret = req.getHeader("Secret-Key");
    String remoteCapabilityStr = req.getHeader("X-Remoting-Capability");
    if (agent == null || secret == null || remoteCapabilityStr == null) {
      LOGGER.warning(() -> "incomplete headers: " + Collections.list(req.getHeaderNames()));
      throw HttpResponses.errorWithoutStack(400, "This endpoint is only for use from agent.jar in WebSocket mode");
    } 
    LOGGER.fine(() -> "receiving headers: " + Collections.list(req.getHeaderNames()));
    if (!JnlpAgentReceiver.DATABASE.exists(agent)) {
      LOGGER.warning(() -> "no such agent " + agent);
      throw HttpResponses.errorWithoutStack(400, "no such agent");
    } 
    if (!MessageDigest.isEqual(secret.getBytes(StandardCharsets.US_ASCII), JnlpAgentReceiver.DATABASE.getSecretOf(agent).getBytes(StandardCharsets.US_ASCII))) {
      LOGGER.warning(() -> "incorrect secret for " + agent);
      throw HttpResponses.forbidden();
    } 
    JnlpConnectionState state = new JnlpConnectionState(null, ExtensionList.lookup(JnlpAgentReceiver.class));
    state.setRemoteEndpointDescription(req.getRemoteAddr());
    state.fireBeforeProperties();
    LOGGER.fine(() -> "connecting " + agent);
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("Node-Name", agent);
    properties.put("Secret-Key", secret);
    String unsafeCookie = req.getHeader("Connection-Cookie");
    if (unsafeCookie != null) {
      cookie = Util.toHexString(Util.fromHexString(unsafeCookie));
    } else {
      cookie = JnlpAgentReceiver.generateCookie();
    } 
    properties.put("JnlpAgentProtocol.cookie", cookie);
    state.fireAfterProperties(Collections.unmodifiableMap(properties));
    Capability remoteCapability = Capability.fromASCII(remoteCapabilityStr);
    LOGGER.fine(() -> "received " + remoteCapability);
    rsp.setHeader("X-Remoting-Capability", (new Capability()).toASCII());
    rsp.setHeader("X-Remoting-Minimum-Version", RemotingVersionInfo.getMinimumSupportedVersion().toString());
    rsp.setHeader("Connection-Cookie", cookie);
    return WebSockets.upgrade(new Session(state, agent, remoteCapability));
  }
}
