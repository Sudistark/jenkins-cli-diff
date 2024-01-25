package jenkins.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public abstract class WebSocketSession {
  private static long PING_INTERVAL_SECONDS = SystemProperties.getLong("jenkins.websocket.pingInterval", Long.valueOf(30L)).longValue();
  
  private static final Logger LOGGER = Logger.getLogger(WebSocketSession.class.getName());
  
  Provider.Handler handler;
  
  private ScheduledFuture<?> pings;
  
  void startPings() {
    if (PING_INTERVAL_SECONDS != 0L)
      this.pings = Timer.get().scheduleAtFixedRate(() -> {
            try {
              Future future = this.handler.sendPing(ByteBuffer.wrap(new byte[0]));
            } catch (Exception x) {
              error(x);
              this.pings.cancel(true);
            } 
          }PING_INTERVAL_SECONDS / 2L, PING_INTERVAL_SECONDS, TimeUnit.SECONDS); 
  }
  
  void stopPings() {
    if (this.pings != null)
      this.pings.cancel(true); 
  }
  
  protected void opened() {}
  
  protected void closed(int statusCode, String reason) {}
  
  protected void error(Throwable cause) { LOGGER.log(Level.WARNING, "unhandled WebSocket service error", cause); }
  
  protected void binary(byte[] payload, int offset, int len) throws IOException { LOGGER.warning("unexpected binary frame"); }
  
  protected void text(String message) throws IOException { LOGGER.warning("unexpected text frame"); }
  
  protected final Future<Void> sendBinary(ByteBuffer data) throws IOException { return this.handler.sendBinary(data); }
  
  protected final void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException { this.handler.sendBinary(partialByte, isLast); }
  
  protected final Future<Void> sendText(String text) throws IOException { return this.handler.sendText(text); }
  
  protected final void close() { this.handler.close(); }
}
