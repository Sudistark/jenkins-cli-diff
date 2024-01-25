package hudson.slaves;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

@Extension
public class ChannelPinger extends ComputerListener {
  static final int PING_TIMEOUT_SECONDS_DEFAULT = 240;
  
  static final int PING_INTERVAL_SECONDS_DEFAULT = 300;
  
  private static final Logger LOGGER = Logger.getLogger(ChannelPinger.class.getName());
  
  private static final String TIMEOUT_SECONDS_PROPERTY = ChannelPinger.class.getName() + ".pingTimeoutSeconds";
  
  private static final String INTERVAL_MINUTES_PROPERTY_DEPRECATED = ChannelPinger.class.getName() + ".pingInterval";
  
  private static final String INTERVAL_SECONDS_PROPERTY = ChannelPinger.class.getName() + ".pingIntervalSeconds";
  
  private int pingTimeoutSeconds = SystemProperties.getInteger(TIMEOUT_SECONDS_PROPERTY, Integer.valueOf(240), Level.WARNING).intValue();
  
  private int pingIntervalSeconds = 300;
  
  public ChannelPinger() {
    Integer interval = SystemProperties.getInteger(INTERVAL_SECONDS_PROPERTY, null, Level.WARNING);
    if (interval == null) {
      interval = SystemProperties.getInteger(INTERVAL_MINUTES_PROPERTY_DEPRECATED, null, Level.WARNING);
      if (interval != null) {
        LOGGER.warning(INTERVAL_MINUTES_PROPERTY_DEPRECATED + " property is deprecated, " + INTERVAL_MINUTES_PROPERTY_DEPRECATED + " should be used");
        interval = Integer.valueOf(interval.intValue() * 60);
      } 
    } 
    if (interval != null)
      this.pingIntervalSeconds = interval.intValue(); 
  }
  
  public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) {
    SlaveComputer slaveComputer = null;
    if (c instanceof SlaveComputer)
      slaveComputer = (SlaveComputer)c; 
    install(channel, slaveComputer);
  }
  
  public void install(Channel channel) { install(channel, null); }
  
  @VisibleForTesting
  void install(Channel channel, @CheckForNull SlaveComputer c) {
    if (this.pingTimeoutSeconds < 1 || this.pingIntervalSeconds < 1) {
      LOGGER.warning("Agent ping is disabled");
      return;
    } 
    try {
      channel.call(new SetUpRemotePing(this.pingTimeoutSeconds, this.pingIntervalSeconds));
      LOGGER.fine("Set up a remote ping for " + channel.getName());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Failed to set up a ping for " + channel.getName(), e);
    } 
    setUpPingForChannel(channel, c, this.pingTimeoutSeconds, this.pingIntervalSeconds, true);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  public static void setUpPingForChannel(Channel channel, SlaveComputer computer, int timeoutSeconds, int intervalSeconds, boolean analysis) {
    LOGGER.log(Level.FINE, "setting up ping on {0} with a {1} seconds interval and {2} seconds timeout", new Object[] { channel.getName(), Integer.valueOf(intervalSeconds), Integer.valueOf(timeoutSeconds) });
    AtomicBoolean isInClosed = new AtomicBoolean(false);
    Object object = new Object(channel, TimeUnit.SECONDS.toMillis(timeoutSeconds), TimeUnit.SECONDS.toMillis(intervalSeconds), analysis, isInClosed, computer, channel);
    channel.addListener(new Object(isInClosed, object));
    object.start();
    LOGGER.log(Level.FINE, "Ping thread started for {0} with a {1} seconds interval and a {2} seconds timeout", new Object[] { channel, 
          Integer.valueOf(intervalSeconds), Integer.valueOf(timeoutSeconds) });
  }
}
