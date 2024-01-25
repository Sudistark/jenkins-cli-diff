package hudson;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.VersionNumber;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.ServerSocketChannel;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.model.identity.InstanceIdentityProvider;
import jenkins.security.stapler.StaplerAccessibleType;
import jenkins.slaves.RemotingVersionInfo;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;

@StaplerAccessibleType
public final class TcpSlaveAgentListener extends Thread {
  private ServerSocketChannel serverSocket;
  
  public final int configuredPort;
  
  public TcpSlaveAgentListener(int port) throws IOException {
    super("TCP agent listener port=" + port);
    this.serverSocket = createSocket(port);
    this.configuredPort = port;
    setUncaughtExceptionHandler((t, e) -> {
          LOGGER.log(Level.SEVERE, "Uncaught exception in TcpSlaveAgentListener " + t, e);
          shutdown();
        });
    LOGGER.log(Level.FINE, "TCP agent listener started on port {0}", Integer.valueOf(getPort()));
    start();
  }
  
  private static ServerSocketChannel createSocket(int port) throws IOException {
    ServerSocketChannel result;
    try {
      result = ServerSocketChannel.open();
      result.socket().bind(new InetSocketAddress(port));
    } catch (BindException e) {
      throw (BindException)(new BindException("Failed to listen on port " + port + " because it's already in use.")).initCause(e);
    } 
    return result;
  }
  
  public int getPort() { return this.serverSocket.socket().getLocalPort(); }
  
  public int getAdvertisedPort() { return (CLI_PORT != null) ? CLI_PORT.intValue() : getPort(); }
  
  public String getAdvertisedHost() {
    if (CLI_HOST_NAME != null)
      return CLI_HOST_NAME; 
    try {
      return (new URL(Jenkins.get().getRootUrl())).getHost();
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Could not get TcpSlaveAgentListener host name", e);
    } 
  }
  
  @Nullable
  public String getIdentityPublicKey() {
    RSAPublicKey key = (RSAPublicKey)InstanceIdentityProvider.RSA.getPublicKey();
    return (key == null) ? null : Base64.getEncoder().encodeToString(key.getEncoded());
  }
  
  public String getAgentProtocolNames() { return String.join(", ", Jenkins.get().getAgentProtocols()); }
  
  public VersionNumber getRemotingMinimumVersion() { return RemotingVersionInfo.getMinimumSupportedVersion(); }
  
  public void run() {
    while (!this.shuttingDown) {
      try {
        Socket s = this.serverSocket.accept().socket();
        s.setKeepAlive(true);
        s.setTcpNoDelay(true);
        (new ConnectionHandler(this, s)).start();
      } catch (Throwable e) {
        if (!this.shuttingDown) {
          LOGGER.log(Level.SEVERE, "Failed to accept TCP connections", e);
          if (!this.serverSocket.isOpen()) {
            LOGGER.log(Level.INFO, "Restarting server socket");
            try {
              this.serverSocket = createSocket(this.configuredPort);
              LOGGER.log(Level.INFO, "TCP agent listener restarted on port {0}", Integer.valueOf(getPort()));
            } catch (IOException ioe) {
              LOGGER.log(Level.WARNING, "Failed to restart server socket", ioe);
            } 
          } 
        } 
      } 
    } 
  }
  
  public void shutdown() {
    this.shuttingDown = true;
    try {
      SocketAddress localAddress = this.serverSocket.getLocalAddress();
      if (localAddress instanceof InetSocketAddress) {
        InetSocketAddress address = (InetSocketAddress)localAddress;
        Socket client = new Socket(address.getHostName(), address.getPort());
        client.setSoTimeout(1000);
        (new PingAgentProtocol()).connect(client);
      } 
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "Failed to send Ping to wake acceptor loop", e);
    } 
    try {
      this.serverSocket.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to close down TCP port", e);
    } 
  }
  
  private static int iotaGen = 1;
  
  private static final Logger LOGGER = Logger.getLogger(TcpSlaveAgentListener.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static String CLI_HOST_NAME = SystemProperties.getString(TcpSlaveAgentListener.class.getName() + ".hostName");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static Integer CLI_PORT = SystemProperties.getInteger(TcpSlaveAgentListener.class.getName() + ".port");
}
