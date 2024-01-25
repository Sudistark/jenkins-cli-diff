package hudson.slaves;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"connectionActivityMonitor"})
public class ConnectionActivityMonitor extends AsyncPeriodicWork {
  public ConnectionActivityMonitor() {
    super("Connection Activity monitoring to agents");
    this.enabled = SystemProperties.getBoolean(ConnectionActivityMonitor.class.getName() + ".enabled");
  }
  
  protected void execute(TaskListener listener) throws IOException, InterruptedException {
    if (!this.enabled)
      return; 
    long now = System.currentTimeMillis();
    for (Computer c : Jenkins.get().getComputers()) {
      VirtualChannel ch = c.getChannel();
      if (ch instanceof Channel) {
        Channel channel = (Channel)ch;
        if (now - channel.getLastHeard() > TIME_TILL_PING) {
          Long lastPing = (Long)channel.getProperty(ConnectionActivityMonitor.class);
          if (lastPing != null && now - lastPing.longValue() > TIMEOUT) {
            LOGGER.info("Repeated ping attempts failed on " + c.getName() + ". Disconnecting");
            c.disconnect(OfflineCause.create(Messages._ConnectionActivityMonitor_OfflineCause()));
          } else {
            channel.callAsync(PING_COMMAND);
            if (lastPing == null)
              channel.setProperty(ConnectionActivityMonitor.class, Long.valueOf(now)); 
          } 
        } else {
          channel.setProperty(ConnectionActivityMonitor.class, null);
        } 
      } 
    } 
  }
  
  public long getRecurrencePeriod() { return this.enabled ? FREQUENCY : TimeUnit.DAYS.toMillis(30L); }
  
  private static final long TIME_TILL_PING = SystemProperties.getLong(ConnectionActivityMonitor.class.getName() + ".timeToPing", Long.valueOf(TimeUnit.MINUTES.toMillis(3L))).longValue();
  
  private static final long FREQUENCY = SystemProperties.getLong(ConnectionActivityMonitor.class.getName() + ".frequency", Long.valueOf(TimeUnit.SECONDS.toMillis(10L))).longValue();
  
  private static final long TIMEOUT = SystemProperties.getLong(ConnectionActivityMonitor.class.getName() + ".timeToPing", Long.valueOf(TimeUnit.MINUTES.toMillis(4L))).longValue();
  
  public boolean enabled;
  
  private static final PingCommand PING_COMMAND = new PingCommand();
  
  private static final Logger LOGGER = Logger.getLogger(ConnectionActivityMonitor.class.getName());
}
