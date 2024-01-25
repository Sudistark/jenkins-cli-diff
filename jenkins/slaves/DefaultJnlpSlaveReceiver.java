package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Functions;
import hudson.TcpSlaveAgentListener;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.ComputerLauncherFilter;
import hudson.slaves.DelegatingComputerLauncher;
import hudson.slaves.SlaveComputer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.ChannelConfigurator;
import jenkins.util.SystemProperties;
import org.jenkinsci.remoting.engine.JnlpConnectionState;
import org.jenkinsci.remoting.protocol.impl.ConnectionRefusalException;
import org.kohsuke.accmod.Restricted;

@Extension
public class DefaultJnlpSlaveReceiver extends JnlpAgentReceiver {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean disableStrictVerification = SystemProperties.getBoolean(DefaultJnlpSlaveReceiver.class.getName() + ".disableStrictVerification");
  
  public boolean owns(String clientName) {
    Computer computer = Jenkins.get().getComputer(clientName);
    return (computer != null);
  }
  
  private static ComputerLauncher getDelegate(ComputerLauncher launcher) {
    try {
      Method getDelegate = launcher.getClass().getMethod("getDelegate", new Class[0]);
      if (ComputerLauncher.class.isAssignableFrom(getDelegate.getReturnType()))
        return (ComputerLauncher)getDelegate.invoke(launcher, new Object[0]); 
    } catch (NoSuchMethodException|java.lang.reflect.InvocationTargetException|IllegalAccessException noSuchMethodException) {}
    try {
      Method getLauncher = launcher.getClass().getMethod("getLauncher", new Class[0]);
      if (ComputerLauncher.class.isAssignableFrom(getLauncher.getReturnType()))
        return (ComputerLauncher)getLauncher.invoke(launcher, new Object[0]); 
    } catch (NoSuchMethodException|java.lang.reflect.InvocationTargetException|IllegalAccessException noSuchMethodException) {}
    return null;
  }
  
  public void afterProperties(@NonNull JnlpConnectionState event) {
    String clientName = event.getProperty("Node-Name");
    SlaveComputer computer = (SlaveComputer)Jenkins.get().getComputer(clientName);
    if (computer == null) {
      event.reject(new ConnectionRefusalException(String.format("%s is not an inbound agent", new Object[] { clientName })));
      return;
    } 
    ComputerLauncher launcher = computer.getLauncher();
    while (!(launcher instanceof hudson.slaves.JNLPLauncher)) {
      if (launcher instanceof DelegatingComputerLauncher) {
        launcher = ((DelegatingComputerLauncher)launcher).getLauncher();
        continue;
      } 
      if (launcher instanceof ComputerLauncherFilter) {
        launcher = ((ComputerLauncherFilter)launcher).getCore();
        continue;
      } 
      ComputerLauncher l;
      if (null != (l = getDelegate(launcher))) {
        LOGGER.log(Level.INFO, "Connecting {0} as an inbound agent where the launcher {1} does not mark itself correctly as being an inbound agent", new Object[] { clientName, computer
              
              .getLauncher().getClass() });
        launcher = l;
        continue;
      } 
      if (disableStrictVerification) {
        LOGGER.log(Level.WARNING, "Connecting {0} as an inbound agent where the launcher {1} does not mark itself correctly as being an inbound agent", new Object[] { clientName, computer
              
              .getLauncher().getClass() });
        break;
      } 
      LOGGER.log(Level.WARNING, "Rejecting connection to {0} from {1} as an inbound agent as the launcher {2} does not extend JNLPLauncher or does not implement DelegatingComputerLauncher with a delegation chain leading to a JNLPLauncher. Set system property jenkins.slaves.DefaultJnlpSlaveReceiver.disableStrictVerification=true to allowconnections until the plugin has been fixed.", new Object[] { clientName, event




            
            .getRemoteEndpointDescription(), computer.getLauncher().getClass() });
      event.reject(new ConnectionRefusalException(String.format("%s is not an inbound agent", new Object[] { clientName })));
      return;
    } 
    Channel ch = computer.getChannel();
    if (ch != null) {
      String cookie = event.getProperty("JnlpAgentProtocol.cookie");
      String channelCookie = (String)ch.getProperty("JnlpAgentProtocol.cookie");
      if (cookie != null && channelCookie != null && MessageDigest.isEqual(cookie.getBytes(StandardCharsets.UTF_8), channelCookie.getBytes(StandardCharsets.UTF_8))) {
        LOGGER.log(Level.INFO, "Disconnecting {0} as we are reconnected from the current peer", clientName);
        try {
          computer.disconnect(new TcpSlaveAgentListener.ConnectionFromCurrentPeer()).get(15L, TimeUnit.SECONDS);
        } catch (ExecutionException|java.util.concurrent.TimeoutException|InterruptedException e) {
          event.reject(new ConnectionRefusalException("Failed to disconnect the current client", e));
          return;
        } 
      } else {
        event.reject(new ConnectionRefusalException(String.format("%s is already connected to this controller. Rejecting this connection.", new Object[] { clientName })));
        return;
      } 
    } 
    event.approve();
    event.setStash(new State(computer));
  }
  
  @SuppressFBWarnings(value = {"OS_OPEN_STREAM"}, justification = "Closed by hudson.slaves.SlaveComputer#kill")
  public void beforeChannel(@NonNull JnlpConnectionState event) {
    State state = (State)event.getStash(State.class);
    SlaveComputer computer = state.getNode();
    OutputStream log = computer.openLogFile();
    state.setLog(log);
    PrintWriter logw = new PrintWriter(new OutputStreamWriter(log, Charset.defaultCharset()), true);
    logw.println("Inbound agent connected from " + event.getRemoteEndpointDescription());
    for (ChannelConfigurator cc : ChannelConfigurator.all())
      cc.onChannelBuilding(event.getChannelBuilder(), computer); 
    event.getChannelBuilder().withHeaderStream(log);
    String cookie = event.getProperty("JnlpAgentProtocol.cookie");
    if (cookie != null)
      event.getChannelBuilder().withProperty("JnlpAgentProtocol.cookie", cookie); 
  }
  
  public void afterChannel(@NonNull JnlpConnectionState event) {
    State state = (State)event.getStash(State.class);
    SlaveComputer computer = state.getNode();
    try {
      computer.setChannel(event.getChannel(), state.getLog(), null);
    } catch (IOException|InterruptedException e) {
      PrintWriter logw = new PrintWriter(new OutputStreamWriter(state.getLog(), Charset.defaultCharset()), true);
      Functions.printStackTrace(e, logw);
      try {
        event.getChannel().close();
      } catch (IOException x) {
        LOGGER.log(Level.WARNING, null, x);
      } 
    } 
  }
  
  public void channelClosed(@NonNull JnlpConnectionState event) {
    String nodeName = event.getProperty("Node-Name");
    IOException cause = event.getCloseCause();
    if (cause instanceof java.nio.channels.ClosedChannelException || cause instanceof hudson.remoting.ChannelClosedException) {
      LOGGER.log(Level.INFO, "{0} for {1} terminated: {2}", new Object[] { Thread.currentThread().getName(), nodeName, cause });
    } else if (cause != null) {
      LOGGER.log(Level.WARNING, Thread.currentThread().getName() + " for " + Thread.currentThread().getName() + " terminated", cause);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(DefaultJnlpSlaveReceiver.class.getName());
}
