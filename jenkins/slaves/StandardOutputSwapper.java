package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

@Extension
public class StandardOutputSwapper extends ComputerListener {
  public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) {
    if (disabled)
      return; 
    try {
      if (((Boolean)channel.call(new ChannelSwapper())).booleanValue())
        listener.getLogger().println("Evacuated stdout"); 
    } catch (Exception x) {
      LOGGER.log(Level.FINE, "Fatal problem swapping file descriptors " + c.getName(), x);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(StandardOutputSwapper.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean disabled = SystemProperties.getBoolean(StandardOutputSwapper.class.getName() + ".disabled");
}
